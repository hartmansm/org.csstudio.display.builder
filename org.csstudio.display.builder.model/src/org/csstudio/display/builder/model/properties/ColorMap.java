/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.model.properties;

/** Map of values to colors
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ColorMap
{
    /** Named color map used for pre-defined values */
    public static class Predefined extends ColorMap
    {
        private final String name, description;

        Predefined(final String name, final String description, final int[][] sections) throws IllegalArgumentException
        {
            super(sections);
            this.name = name;
            this.description = description;
        }

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }

        @Override
        public String toString()
        {
            return name;
        }
    }

    /** 'Viridis' color map by Eric Firing
     *
     *  <p>Perceptually uniform, best ever color map.
     *  Google "matplotlib viridis".
     */
    // Values obtained via
    //
    // import matplotlib.pyplot as plt
    // map = plt.get_cmap('viridis')
    // for i,c in enumerate(map.colors):
    //     print("{ %4d, %3d, %3d, %3d }," % ( i, c[0]*255, c[1]*255, c[2]*255) )
    public final static Predefined VIRIDIS = new Predefined("VIRIDIS", "Viridis (perceptually uniform)", new int[][]
    {
       {   0,  68,   1,  84 },
       {   1,  68,   2,  85 },
       {   2,  68,   3,  87 },
       {   3,  69,   5,  88 },
       {   4,  69,   6,  90 },
       {   5,  69,   8,  91 },
       {   6,  70,   9,  92 },
       {   7,  70,  11,  94 },
       {   8,  70,  12,  95 },
       {   9,  70,  14,  97 },
       {  10,  71,  15,  98 },
       {  11,  71,  17,  99 },
       {  12,  71,  18, 101 },
       {  13,  71,  20, 102 },
       {  14,  71,  21, 103 },
       {  15,  71,  22, 105 },
       {  16,  71,  24, 106 },
       {  17,  72,  25, 107 },
       {  18,  72,  26, 108 },
       {  19,  72,  28, 110 },
       {  20,  72,  29, 111 },
       {  21,  72,  30, 112 },
       {  22,  72,  32, 113 },
       {  23,  72,  33, 114 },
       {  24,  72,  34, 115 },
       {  25,  72,  35, 116 },
       {  26,  71,  37, 117 },
       {  27,  71,  38, 118 },
       {  28,  71,  39, 119 },
       {  29,  71,  40, 120 },
       {  30,  71,  42, 121 },
       {  31,  71,  43, 122 },
       {  32,  71,  44, 123 },
       {  33,  70,  45, 124 },
       {  34,  70,  47, 124 },
       {  35,  70,  48, 125 },
       {  36,  70,  49, 126 },
       {  37,  69,  50, 127 },
       {  38,  69,  52, 127 },
       {  39,  69,  53, 128 },
       {  40,  69,  54, 129 },
       {  41,  68,  55, 129 },
       {  42,  68,  57, 130 },
       {  43,  67,  58, 131 },
       {  44,  67,  59, 131 },
       {  45,  67,  60, 132 },
       {  46,  66,  61, 132 },
       {  47,  66,  62, 133 },
       {  48,  66,  64, 133 },
       {  49,  65,  65, 134 },
       {  50,  65,  66, 134 },
       {  51,  64,  67, 135 },
       {  52,  64,  68, 135 },
       {  53,  63,  69, 135 },
       {  54,  63,  71, 136 },
       {  55,  62,  72, 136 },
       {  56,  62,  73, 137 },
       {  57,  61,  74, 137 },
       {  58,  61,  75, 137 },
       {  59,  61,  76, 137 },
       {  60,  60,  77, 138 },
       {  61,  60,  78, 138 },
       {  62,  59,  80, 138 },
       {  63,  59,  81, 138 },
       {  64,  58,  82, 139 },
       {  65,  58,  83, 139 },
       {  66,  57,  84, 139 },
       {  67,  57,  85, 139 },
       {  68,  56,  86, 139 },
       {  69,  56,  87, 140 },
       {  70,  55,  88, 140 },
       {  71,  55,  89, 140 },
       {  72,  54,  90, 140 },
       {  73,  54,  91, 140 },
       {  74,  53,  92, 140 },
       {  75,  53,  93, 140 },
       {  76,  52,  94, 141 },
       {  77,  52,  95, 141 },
       {  78,  51,  96, 141 },
       {  79,  51,  97, 141 },
       {  80,  50,  98, 141 },
       {  81,  50,  99, 141 },
       {  82,  49, 100, 141 },
       {  83,  49, 101, 141 },
       {  84,  49, 102, 141 },
       {  85,  48, 103, 141 },
       {  86,  48, 104, 141 },
       {  87,  47, 105, 141 },
       {  88,  47, 106, 141 },
       {  89,  46, 107, 142 },
       {  90,  46, 108, 142 },
       {  91,  46, 109, 142 },
       {  92,  45, 110, 142 },
       {  93,  45, 111, 142 },
       {  94,  44, 112, 142 },
       {  95,  44, 113, 142 },
       {  96,  44, 114, 142 },
       {  97,  43, 115, 142 },
       {  98,  43, 116, 142 },
       {  99,  42, 117, 142 },
       { 100,  42, 118, 142 },
       { 101,  42, 119, 142 },
       { 102,  41, 120, 142 },
       { 103,  41, 121, 142 },
       { 104,  40, 122, 142 },
       { 105,  40, 122, 142 },
       { 106,  40, 123, 142 },
       { 107,  39, 124, 142 },
       { 108,  39, 125, 142 },
       { 109,  39, 126, 142 },
       { 110,  38, 127, 142 },
       { 111,  38, 128, 142 },
       { 112,  38, 129, 142 },
       { 113,  37, 130, 142 },
       { 114,  37, 131, 141 },
       { 115,  36, 132, 141 },
       { 116,  36, 133, 141 },
       { 117,  36, 134, 141 },
       { 118,  35, 135, 141 },
       { 119,  35, 136, 141 },
       { 120,  35, 137, 141 },
       { 121,  34, 137, 141 },
       { 122,  34, 138, 141 },
       { 123,  34, 139, 141 },
       { 124,  33, 140, 141 },
       { 125,  33, 141, 140 },
       { 126,  33, 142, 140 },
       { 127,  32, 143, 140 },
       { 128,  32, 144, 140 },
       { 129,  32, 145, 140 },
       { 130,  31, 146, 140 },
       { 131,  31, 147, 139 },
       { 132,  31, 148, 139 },
       { 133,  31, 149, 139 },
       { 134,  31, 150, 139 },
       { 135,  30, 151, 138 },
       { 136,  30, 152, 138 },
       { 137,  30, 153, 138 },
       { 138,  30, 153, 138 },
       { 139,  30, 154, 137 },
       { 140,  30, 155, 137 },
       { 141,  30, 156, 137 },
       { 142,  30, 157, 136 },
       { 143,  30, 158, 136 },
       { 144,  30, 159, 136 },
       { 145,  30, 160, 135 },
       { 146,  31, 161, 135 },
       { 147,  31, 162, 134 },
       { 148,  31, 163, 134 },
       { 149,  32, 164, 133 },
       { 150,  32, 165, 133 },
       { 151,  33, 166, 133 },
       { 152,  33, 167, 132 },
       { 153,  34, 167, 132 },
       { 154,  35, 168, 131 },
       { 155,  35, 169, 130 },
       { 156,  36, 170, 130 },
       { 157,  37, 171, 129 },
       { 158,  38, 172, 129 },
       { 159,  39, 173, 128 },
       { 160,  40, 174, 127 },
       { 161,  41, 175, 127 },
       { 162,  42, 176, 126 },
       { 163,  43, 177, 125 },
       { 164,  44, 177, 125 },
       { 165,  46, 178, 124 },
       { 166,  47, 179, 123 },
       { 167,  48, 180, 122 },
       { 168,  50, 181, 122 },
       { 169,  51, 182, 121 },
       { 170,  53, 183, 120 },
       { 171,  54, 184, 119 },
       { 172,  56, 185, 118 },
       { 173,  57, 185, 118 },
       { 174,  59, 186, 117 },
       { 175,  61, 187, 116 },
       { 176,  62, 188, 115 },
       { 177,  64, 189, 114 },
       { 178,  66, 190, 113 },
       { 179,  68, 190, 112 },
       { 180,  69, 191, 111 },
       { 181,  71, 192, 110 },
       { 182,  73, 193, 109 },
       { 183,  75, 194, 108 },
       { 184,  77, 194, 107 },
       { 185,  79, 195, 105 },
       { 186,  81, 196, 104 },
       { 187,  83, 197, 103 },
       { 188,  85, 198, 102 },
       { 189,  87, 198, 101 },
       { 190,  89, 199, 100 },
       { 191,  91, 200,  98 },
       { 192,  94, 201,  97 },
       { 193,  96, 201,  96 },
       { 194,  98, 202,  95 },
       { 195, 100, 203,  93 },
       { 196, 103, 204,  92 },
       { 197, 105, 204,  91 },
       { 198, 107, 205,  89 },
       { 199, 109, 206,  88 },
       { 200, 112, 206,  86 },
       { 201, 114, 207,  85 },
       { 202, 116, 208,  84 },
       { 203, 119, 208,  82 },
       { 204, 121, 209,  81 },
       { 205, 124, 210,  79 },
       { 206, 126, 210,  78 },
       { 207, 129, 211,  76 },
       { 208, 131, 211,  75 },
       { 209, 134, 212,  73 },
       { 210, 136, 213,  71 },
       { 211, 139, 213,  70 },
       { 212, 141, 214,  68 },
       { 213, 144, 214,  67 },
       { 214, 146, 215,  65 },
       { 215, 149, 215,  63 },
       { 216, 151, 216,  62 },
       { 217, 154, 216,  60 },
       { 218, 157, 217,  58 },
       { 219, 159, 217,  56 },
       { 220, 162, 218,  55 },
       { 221, 165, 218,  53 },
       { 222, 167, 219,  51 },
       { 223, 170, 219,  50 },
       { 224, 173, 220,  48 },
       { 225, 175, 220,  46 },
       { 226, 178, 221,  44 },
       { 227, 181, 221,  43 },
       { 228, 183, 221,  41 },
       { 229, 186, 222,  39 },
       { 230, 189, 222,  38 },
       { 231, 191, 223,  36 },
       { 232, 194, 223,  34 },
       { 233, 197, 223,  33 },
       { 234, 199, 224,  31 },
       { 235, 202, 224,  30 },
       { 236, 205, 224,  29 },
       { 237, 207, 225,  28 },
       { 238, 210, 225,  27 },
       { 239, 212, 225,  26 },
       { 240, 215, 226,  25 },
       { 241, 218, 226,  24 },
       { 242, 220, 226,  24 },
       { 243, 223, 227,  24 },
       { 244, 225, 227,  24 },
       { 245, 228, 227,  24 },
       { 246, 231, 228,  25 },
       { 247, 233, 228,  25 },
       { 248, 236, 228,  26 },
       { 249, 238, 229,  27 },
       { 250, 241, 229,  28 },
       { 251, 243, 229,  30 },
       { 252, 246, 230,  31 },
       { 253, 248, 230,  33 },
       { 254, 250, 230,  34 },
       { 255, 253, 231,  36 }
    });

    public final static Predefined GRAY = new Predefined("GRAY", "Gray Scale", new int[][]
    {
        {   0,   0,   0,   0 },
        { 255, 255, 255, 255 }
    });

    public final static Predefined JET = new Predefined("JET", "Jet", new int[][]
    {
        {   0,   0,   0, 143 },
        {  28,   0,   0, 255 },
        {  93,   0, 255, 255 },
        { 158, 255, 255,   0 },
        { 223, 255,   0,   0 },
        { 255, 128,   0,   0 }
    });

    public final static Predefined SPECTRUM = new Predefined("SPECTRUM", "Color Spectrum", new int[][]
    {
        {   0,   0,   0,   0 },
        {  32, 255,   0, 255 },
        {  64,   0,   0, 255 },
        {  96,   0, 255, 255 },
        { 128,   0, 255,   0 },
        { 160, 255, 255,   0 },
        { 190, 255, 128,   0 },
        { 223, 255,   0,   0 },
        { 255, 255, 255, 255 }
    });

    public final static Predefined HOT = new Predefined("HOT", "Hot", new int[][]
    {
        {   0,  11,   0,   0 },
        {  94, 255,   0,   0 },
        { 190, 255, 255,   0 },
        { 255, 255, 255, 255 }
    });

    public final static Predefined COOL = new Predefined("COOL", "Cool", new int[][]
    {
        {   0,   0, 255, 255 },
        { 255, 255,   0, 255 }
    });

    public final static Predefined SHADED = new Predefined("SHADED", "Shaded", new int[][]
    {
        {   0,   0,   0,   0 },
        { 128, 255,   0,   0 },
        { 255, 255, 255, 255 }
    });

    public static Predefined[] PREDEFINED = new Predefined[]
    {
        VIRIDIS, GRAY, JET, SPECTRUM, HOT, COOL, SHADED
    };


    private final int[][] sections;
    private final WidgetColor[] colors = new WidgetColor[256];

    /** Initialize color map based on 'sections'.
     *
     *  <p>Each sections is a 4-tuple with these elements:
     *  <ul>
     *  <li>section[i][0]: Intensity level 0...255
     *  <li>section[i][1]: 'Red' 0..255
     *  <li>section[i][2]: 'Green' 0..255
     *  <li>section[i][3]: 'Blue' 0..255
     *  </ul>
     *  The color of a 'section' is used for the given
     *  Intensity level up to the intensity level of the
     *  next section.
     *  The first section, section[0], must have Intensity 0.
     *  The last section must have Intensity 255.
     *  Sections must be ordered by Intensity.
     *
     *  @param sections Sections of the map.
     *  @throws IllegalArgumentException on error
     */
    public ColorMap(final int sections[][]) throws IllegalArgumentException
    {
        if (sections.length < 2)
            throw new IllegalArgumentException("Need at least 2 sections for '0' and '255'");
        if (sections[0][0] != 0)
            throw new IllegalArgumentException("Intensity of first section must be 0");
        if (sections[sections.length-1][0] != 255)
            throw new IllegalArgumentException("Intensity of last section must be 255");

        this.sections = sections;
        for (int i = 0; i<sections.length-1; ++i)
        {
            final int start_intensity = sections[i][0];
            final int end_intensity = sections[i+1][0];
            for (int c=start_intensity; c<end_intensity; ++c)
                colors[c] = new WidgetColor(
                                interpolate(start_intensity, sections[i][1], end_intensity, sections[i+1][1], c),  // red
                                interpolate(start_intensity, sections[i][2], end_intensity, sections[i+1][2], c),  // green
                                interpolate(start_intensity, sections[i][3], end_intensity, sections[i+1][3], c)); // blue
        }
        // Loop goes just up to last section, not including last section
        final int last = sections.length - 1;
        colors[255] = new WidgetColor(sections[last][1], sections[last][2], sections[last][3]);
    }

    /** @return Sections, i.e. 4-tuples with these elements:
     *  <ul>
     *  <li>section[i][0]: Intensity level 0...255
     *  <li>section[i][1]: 'Red' 0..255
     *  <li>section[i][2]: 'Green' 0..255
     *  <li>section[i][3]: 'Blue' 0..255
     *  </ul>
     */
    public int[][] getSections()
    {
        return sections;
    }

    /** @param intensity Intensity 0 to 255
     *  @return Color for that intensity
     */
    public WidgetColor getColor(final int intensity)
    {
        if (intensity <= 0)
            return colors[0];
        if (intensity >= 255)
            return colors[255];
        return colors[intensity];
    }

    /** @param intensity Intensity 0.0 to 1.0
     *  @return Color for that intensity
     */
    public WidgetColor getColor(final double intensity)
    {
        // Rounds to nearest of the 255 colors.
        // Could interpolate between them..
        return getColor((int) (intensity * 255.0 + 0.5));
    }

    /** Linear interpolation between two points (x/y)
     *
     * @param x0 One endpoint
     * @param y0
     * @param x0 Other endpoint
     * @param y1
     * @param x Desired location
     * @return Value at that location
     */
    private static int interpolate(final int x0,  final int y0, final int x1, final int y1, final int x)
    {
        // Avoid div/0
        if (x0 == x1)
            return y0;
        return y0 +  (y1 - y0) * (x - x0) / (x1 - x0);
    }
}
