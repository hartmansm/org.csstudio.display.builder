/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.trends.databrowser3.waveformview;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.csstudio.archive.vtype.TimestampHelper;
import org.csstudio.archive.vtype.VTypeHelper;
import org.csstudio.javafx.rtplot.PointType;
import org.csstudio.javafx.rtplot.RTValuePlot;
import org.csstudio.javafx.rtplot.Trace;
import org.csstudio.javafx.rtplot.TraceType;
import org.csstudio.javafx.rtplot.YAxis;
import org.csstudio.javafx.rtplot.data.TimeDataSearch;
import org.csstudio.javafx.swt.JFX_SWT_Wrapper;
import org.csstudio.trends.databrowser3.Activator;
import org.csstudio.trends.databrowser3.Messages;
import org.csstudio.trends.databrowser3.editor.DataBrowserAwareView;
import org.csstudio.trends.databrowser3.editor.ToggleLegendAction;
import org.csstudio.trends.databrowser3.editor.ToggleToolbarAction;
import org.csstudio.trends.databrowser3.model.AnnotationInfo;
import org.csstudio.trends.databrowser3.model.Model;
import org.csstudio.trends.databrowser3.model.ModelItem;
import org.csstudio.trends.databrowser3.model.ModelListener;
import org.csstudio.trends.databrowser3.model.ModelListenerAdapter;
import org.csstudio.trends.databrowser3.model.PlotSample;
import org.csstudio.trends.databrowser3.model.PlotSamples;
import org.csstudio.ui.util.widgets.MultipleSelectionCombo;
import org.diirt.vtype.VNumberArray;
import org.diirt.vtype.VType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;

import javafx.geometry.Point2D;
import javafx.scene.Scene;

/** View for inspecting Waveform (Array) Samples of the current Model
 *  @author Kay Kasemir
 *  @author Will Rogers Show current waveform sample in plot, various bugfixes
 *  @author Takashi Nakamoto changed WaveformView to handle multiple items with
 *                           the same name.
 *  @author Xihui Chen (Added some work around to make it work for rap).
 */
@SuppressWarnings("nls")
public class WaveformView extends DataBrowserAwareView
{
    private class ToggleYAxisAction extends Action
    {
        public ToggleYAxisAction()
        {
            updateText();
        }

        public void updateText()
        {
            setText(plot.getYAxes().get(0).isLogarithmic() ? "Linear Axis" : "Logarithmic Axis");
        }

        @Override
        public void run()
        {
            plot.getYAxes().get(0).setLogarithmic(!plot.getYAxes().get(0).isLogarithmic());
            plot.requestLayout();
            updateText();
        }
    }

    /** View ID registered in plugin.xml */
    final public static String ID = "org.csstudio.trends.databrowser.waveformview.WaveformView";

    /** Text used for the annotation that indicates waveform sample */
    final public static String ANNOTATION_TEXT = "Waveform view";

    /** PV Name(s) selector */
    private MultipleSelectionCombo<ModelItem> pv_select;

    /** Plot */
    private RTValuePlot plot;

    /** Selector for first model_item's current sample */
    private Slider sample_index;

    /** Timestamp of current sample. */
    private Text timestamp;

    /** Status/severity of current sample. */
    private Text status;

    /** Model of the currently active Data Browser plot or <code>null</code> */
    private Model model;

    /** Annotation in plot that indicates waveform sample */
    private List<AnnotationInfo> waveform_annotations;

    private boolean changing_annotations = false;

    private final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();

    private ScheduledFuture<?> pending_move = null;

    final private ModelListener model_listener = new ModelListenerAdapter()
    {
        @Override
        public void itemAdded(final ModelItem item)
        {
            update(false);
        }

        @Override
        public void itemRemoved(final ModelItem item)
        {
            model_items.remove(item);
            // Will update the combo to reflect missing item,
            // then detect model_item change and selectPV(null)
            update(false);
        }

        @Override
        public void changedItemLook(final ModelItem item)
        {
            update(false);
        }

        @Override
        public void changedAnnotations()
        {
            if (changing_annotations)
                return;

            // Reacting as the user moves the annotation
            // would be too expensive.
            // Delay, canceling previous request, for "post-selection"
            // type update once the user stops moving the annotation for a little time
            if (pending_move != null)
                pending_move.cancel(false);
            pending_move = timer.schedule(WaveformView.this::userMovedAnnotation, 500, TimeUnit.MILLISECONDS);
        }

        @Override
        public void changedTimerange()
        {
            // Update selected sample to assert that it's one of the visible ones.
            if (model_items != null)
                showSelectedSample();
        }
    };

    /** Selected model item in model, or <code>null</code> */
    private List<ModelItem> model_items = null;

    /** Waveform for the currently selected sample */
    private List<WaveformValueDataProvider> waveforms = null;

    /** {@inheritDoc} */
    @Override
    protected void doCreatePartControl(final Composite parent)
    {
        // Arrange disposal
        parent.addDisposeListener((DisposeEvent e) ->
        {   // Ignore current model after this view is disposed.
            if (model != null)
            {
                model.removeListener(model_listener);
                removeAnnotation();
            }
        });

        final GridLayout layout = new GridLayout(4, false);
        parent.setLayout(layout);

        // PV: .pvs..... [Refresh]
        // =====================
        // ======= Plot ========
        // =====================
        // <<<<<< Slider >>>>>>
        // Timestamp: __________ Sevr./Status: __________

        // PV: .pvs..... [Refresh]
        Label l = new Label(parent, 0);
        l.setText(Messages.SampleView_Item);
        l.setLayoutData(new GridData());

        pv_select = new MultipleSelectionCombo<>(parent, 0);
        pv_select.setItems(getAvailableItems());
        pv_select.setLayoutData(new GridData(SWT.FILL, 0, true, false, layout.numColumns-2, 1));
        pv_select.addPropertyChangeListener(event ->
        {
            if (event.getPropertyName().equals("selection"))
                selectPV(pv_select.getSelection());
        });

        final Button refresh = new Button(parent, SWT.PUSH);
        refresh.setText(Messages.SampleView_Refresh);
        refresh.setToolTipText(Messages.SampleView_RefreshTT);
        refresh.setLayoutData(new GridData());
        refresh.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(final SelectionEvent e)
            {
                selectPV(pv_select.getSelection());
            }
        });

        // =====================
        // ======= Plot ========
        // =====================
        final JFX_SWT_Wrapper wrapper = new JFX_SWT_Wrapper(parent, () ->
        {
            plot = new RTValuePlot(true);
            plot.getXAxis().setName(Messages.WaveformIndex);
            plot.getYAxes().get(0).setAutoscale(true);
            plot.getYAxes().get(0).useAxisName(false);
            plot.showLegend(false);
            plot.requestUpdate();
            return new Scene(plot);
        });
        final Control plot_canvas = wrapper.getFXCanvas();
        plot_canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, layout.numColumns, 1));

        // <<<<<< Slider >>>>>>
        sample_index = new Slider(parent, SWT.HORIZONTAL);
        sample_index.setToolTipText(Messages.WaveformTimeSelector);
        sample_index.setLayoutData(new GridData(SWT.FILL, 0, true, false, layout.numColumns, 1));
        sample_index.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                showSelectedSample();
            }
        });

        // Timestamp: __________ Sevr./Status: __________
        l = new Label(parent, 0);
        l.setText(Messages.WaveformTimestamp);
        l.setLayoutData(new GridData());
        timestamp = new Text(parent, SWT.BORDER | SWT.READ_ONLY);
        timestamp.setLayoutData(new GridData(SWT.FILL, 0, true, false));

        l = new Label(parent, 0);
        l.setText(Messages.WaveformStatus);
        l.setLayoutData(new GridData());
        status = new Text(parent, SWT.BORDER | SWT.READ_ONLY);
        status.setLayoutData(new GridData(SWT.FILL, 0, true, false));

        // Context Menu
        final MenuManager mm = new MenuManager();
        mm.setRemoveAllWhenShown(true);

        final Menu menu = mm.createContextMenu(plot_canvas);
        plot_canvas.setMenu(menu);
        getSite().registerContextMenu(mm, null);
        mm.addMenuListener(manager ->
        {
            mm.add(new ToggleToolbarAction(plot));
            mm.add(new ToggleLegendAction(plot));
            mm.add(new Separator());
            mm.add(new ToggleYAxisAction());
        });
    }

    /** @returns Current items in model */
    private List<ModelItem> getAvailableItems()
    {

        final List<ModelItem> curr_names_list = new ArrayList<>();
        if (model != null)
            for (ModelItem item : model.getItems())
                curr_names_list.add(item);
        return curr_names_list;
     }

    /** {@inheritDoc} */
    @Override
    public void setFocus()
    {
        pv_select.setFocus();
    }

    /** {@inheritDoc} */
    @Override
    protected void updateModel(final Model old_model, final Model model)
    {
        if (this.model == model)
            return;
        removeAnnotation();
        this.model = model;
        if (old_model != model)
        {
            if (old_model != null)
                old_model.removeListener(model_listener);

            if (model != null)
                model.addListener(model_listener);
        }
        update(old_model != model);
    }

    /** Update combo box of this view.
     *  Since it interacts with the UI run on the UI thread.
     *  @param model_changed Is this a different model?
     */
    private void update(final boolean model_changed)
    {
        pv_select.getDisplay().asyncExec( () ->
        {
            if (pv_select.isDisposed())
                return;

            final List<ModelItem> old_selection = new ArrayList<>(pv_select.getSelection());
            pv_select.setItems(getAvailableItems());
            if (model == null)
            {   // Clear/disable GUI
                pv_select.setEnabled(false);
                selectPV(null);
                return;
            }

            // Show new items, clear rest
            pv_select.setEnabled(true);
            selectPV(old_selection);
        });
    }

    /** Select given PV item (or <code>null</code>). */
    private void selectPV(final List<ModelItem> new_item)
    {
        // Delete all existing traces
        for (Trace<Double> trace : plot.getTraces())
            plot.removeTrace(trace);

        model_items = new_item;

        // No or unknown PV name?
        removeAnnotation();

        if (model_items == null  ||  model_items.isEmpty())
        {
            sample_index.setEnabled(false);
            return;
        }

        // Prepare to show waveforms of model item in plot
        waveforms = new ArrayList<>();

        // Create traces for waveforms
        for (ModelItem item : model_items)
        {
            final WaveformValueDataProvider waveform = new WaveformValueDataProvider();
            waveforms.add(waveform);
            plot.addTrace(item.getResolvedDisplayName(), item.getUnits(), waveform, item.getPaintColor(), TraceType.NONE, 1, PointType.CIRCLES, 5, 0);
        }

        // Enable waveform selection and update slider's range
        sample_index.setEnabled(true);
        showSelectedSample();
        // Autoscale Y axis by default.
        for (YAxis<Double> yaxis : plot.getYAxes())
            yaxis.setAutoscale(true);
    }

    /** Show the current sample of the current model item. */
    private void showSelectedSample()
    {
        final int numItems = model_items.size();

        final int idx = sample_index.getSelection();

        String timestampText = "";
        String statusText = "";

        Instant firstWaveformSampleTime = null;

        int n = 0;
        for (ModelItem model_item : model_items)
        {
            // Get selected sample (= one waveform)
            final PlotSamples samples = model_item.getSamples();
            PlotSample sample;
            samples.getLock().lock();
            try
            {
                if (n == 0)
                {
                    sample_index.setMaximum(samples.size());
                    sample = samples.get(idx);
                }
                else
                {
                    sample = samples.get(0);
                    for (int s=1; s<samples.size(); ++s)
                    {
                        if (VTypeHelper.getTimestamp(samples.get(s).getVType()).isAfter(firstWaveformSampleTime))
                        {
                            sample = samples.get(s-1);
                            break;
                        }
                    }
                }
            }
            finally
            {
                samples.getLock().unlock();
            }
            // Setting the value can be delayed while the plot is being updated
            final VType value = sample.getVType();
            final int waveformIndex = n;
            Activator.getThreadPool().execute(() -> waveforms.get(waveformIndex).setValue(value));
            if (value == null)
                clearInfo();
            else
            {
                updateAnnotation(n, sample.getPosition(), sample.getValue());
                if (n == 0)
                {
                    int size = value instanceof VNumberArray ? ((VNumberArray)value).getData().size() : 1;
                    plot.getXAxis().setValueRange(0.0, (double)size);
                    firstWaveformSampleTime = VTypeHelper.getTimestamp(value);
                    timestampText += TimestampHelper.format(firstWaveformSampleTime);
                    statusText += NLS.bind(Messages.SeverityStatusFmt, VTypeHelper.getSeverity(value).toString(), VTypeHelper.getMessage(value));
                }
                else
                {
                    timestampText += "; " + TimestampHelper.format(firstWaveformSampleTime);
                    statusText += "; " + NLS.bind(Messages.SeverityStatusFmt, VTypeHelper.getSeverity(value).toString(), VTypeHelper.getMessage(value));
                }
            }
            ++n;
        }
        timestamp.setText(timestampText);
        status.setText(statusText);
        plot.requestUpdate();
    }

    /** Clear all the info fields. */
    private void clearInfo()
    {
        timestamp.setText("");
        status.setText("");
        removeAnnotation();
    }

    private void userMovedAnnotation()
    {
        if (waveform_annotations == null)
            return;
        for (AnnotationInfo annotation : model.getAnnotations())
        {   // Locate the annotation for this waveform
            for (AnnotationInfo waveform_annotation : waveform_annotations)
            {
                if (annotation.isInternal()  &&
                    annotation.getItemIndex() == waveform_annotation.getItemIndex() &&
                    annotation.getText().equals(waveform_annotation.getText()))
                {   // Locate index of sample for annotation's time stamp
                    // by first locating the relevant samples
                    for (ModelItem model_item : model_items)
                        if (annotation.getText().contains(model_item.getDisplayName()))
                        {
                            final PlotSamples samples = model_item.getSamples();
                            final TimeDataSearch search = new TimeDataSearch();
                            final int idx;
                            samples.getLock().lock();
                            try
                            {
                                idx = search.findClosestSample(samples, annotation.getTime());
                            }
                            finally
                            {
                                samples.getLock().unlock();
                            }
                            // Update waveform view for that sample on UI thread
                            sample_index.getDisplay().asyncExec(() ->
                            {
                                sample_index.setSelection(idx);
                                showSelectedSample();
                            });
                            return;
                        }
                }
            }
        }
    }

    private void removeAnnotation()
    {
        if (model != null && waveform_annotations != null)
        {
            final List<AnnotationInfo> modelAnnotations = new ArrayList<AnnotationInfo>(model.getAnnotations());
            for (AnnotationInfo waveform_annotation : waveform_annotations)
                if (modelAnnotations.remove(waveform_annotation))
                {
                    changing_annotations = true;
                    model.setAnnotations(modelAnnotations);
                    changing_annotations = false;
                }
        }
        waveform_annotations = null;
    }

    private void updateAnnotation(final int annotation_index, final Instant time, final double value)
    {
        if (waveform_annotations == null)
            waveform_annotations = new ArrayList<AnnotationInfo>();

        final List<AnnotationInfo> annotations = new ArrayList<AnnotationInfo>(model.getAnnotations());
        // Initial annotation offset
        Point2D offset = new Point2D(20, -20);
        // If already in model, note its offset and remove
        for (AnnotationInfo annotation : annotations)
        {
            if (annotation.getText().equals(buildAnnotationText(annotation_index)))
            {   // Update offset to where user last placed it
                offset = annotation.getOffset();
                annotations.remove(annotation);
                break;
            }
        }

        int i = 0;
        int item_index = 0;
        for (ModelItem item : model.getItems())
        {
            if (item == model_items.get(annotation_index))
            {
                item_index = i;
                break;
            }
            i++;
        }
        waveform_annotations.add(annotation_index, new AnnotationInfo(true, item_index, time, value, offset, buildAnnotationText(annotation_index)));
        annotations.add(waveform_annotations.get(annotation_index));
        changing_annotations = true;
        model.setAnnotations(annotations);
        changing_annotations = false;
    }

    private String buildAnnotationText(final int annotation_index)
    {
        return ANNOTATION_TEXT + " " + model_items.get(annotation_index).getDisplayName();
    }
}
