package net.sknv.game;

import net.sknv.engine.Utils;
import net.sknv.engine.graph.Material;
import net.sknv.engine.graph.Mesh;
import net.sknv.engine.graph.Texture;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Platform;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.*;
import static org.lwjgl.BufferUtils.createByteBuffer;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memSlice;

public class TrueType {

    private final ByteBuffer ttf;
    private final STBTTFontinfo info;
    private final int ascent;
    private final int descent;
    private final int lineGap;
    private int fontHeight = 24;
    private float contentScaleY;
    private float contentScaleX;

    private String text = "placeholder";
    private final   int    lineCount;

    private int ww = 800;
    private int wh = 600;

    private int   scale = 1;
    private int   lineOffset = 0;
    private float lineHeight = 0;

    private boolean kerningEnabled = true;
    private boolean lineBBEnabled = false;

    private int BITMAP_SIZE = 352;
    private int BITMAP_W;
    private int BITMAP_H;
    private Texture bitmapTexture;
    private final STBTTBakedChar.Buffer cdata = STBTTBakedChar.malloc(352);

    public TrueType(long window){
        try {//todo: hardcode
            this.ttf = ioResourceToByteBuffer("src/main/resources/fonts/Roboto-Regular.ttf", 168 * 1024);
        } catch (Exception e) {
            System.out.println("failed to load font");
            throw new RuntimeException(e);
        }

        info = STBTTFontinfo.create();
        STBTruetype.stbtt_InitFont(info, ttf);

        try (MemoryStack stack = stackPush()) {
            IntBuffer pAscent  = stack.mallocInt(1);
            IntBuffer pDescent = stack.mallocInt(1);
            IntBuffer pLineGap = stack.mallocInt(1);

            stbtt_GetFontVMetrics(info, pAscent, pDescent, pLineGap);

            ascent = pAscent.get(0);
            descent = pDescent.get(0);
            lineGap = pLineGap.get(0);
        }

        Matcher m = Pattern.compile("^.*$", Pattern.MULTILINE).matcher(text);
        int lc = 0;
        while (m.find()) {
            lc++;
        }
        lineCount = lc;

        //set content scale
        glfwMakeContextCurrent(window);
        try (MemoryStack s = stackPush()) {
            FloatBuffer px = s.mallocFloat(1);
            FloatBuffer py = s.mallocFloat(1);

            glfwGetMonitorContentScale(glfwGetPrimaryMonitor(), px, py);

            contentScaleX = px.get(0);
            contentScaleY = py.get(0);
        }

        //BakeBitMapTexure todo: ensure bitmap texture contains all characters, if it doesnt -> weird behaviour
        BITMAP_W = round(BITMAP_SIZE * contentScaleX);
        BITMAP_H = round(BITMAP_SIZE * contentScaleY);

        ByteBuffer bitmap = BufferUtils.createByteBuffer(BITMAP_W * BITMAP_H);
        stbtt_BakeFontBitmap(ttf, fontHeight * contentScaleY, bitmap, BITMAP_W, BITMAP_H, 32, cdata);

        bitmapTexture = new Texture(bitmap, BITMAP_W, BITMAP_H);
    }

    public STBTTFontinfo getInfo() {
        return info;
    }
    public int getScale() {
        return scale;
    }
    public float getContentScaleX() {
        return contentScaleX;
    }
    public float getContentScaleY() {
        return contentScaleY;
    }
    public int getFontHeight() {
        return fontHeight;
    }
    public int getLineOffset() {
        return lineOffset;
    }
    private boolean isLineBBEnabled() {
        return lineBBEnabled;
    }
    private boolean isKerningEnabled() {
        return kerningEnabled;
    }
    private void setLineOffset(float offset) {
        setLineOffset(round(offset));
    }
    private void setLineOffset(int offset) {
        lineOffset = max(0, min(offset, lineCount - (int)(wh / lineHeight)));
    }

    public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {
        ByteBuffer buffer;

        Path path = Paths.get(resource);
        if (Files.isReadable(path)) {
            try (SeekableByteChannel fc = Files.newByteChannel(path)) {
                buffer = createByteBuffer((int)fc.size() + 1);
                while (fc.read(buffer) != -1) {
                    ;
                }
            }
        } else {
            try (
                    InputStream source = TrueType.class.getClassLoader().getResourceAsStream(resource);
                    ReadableByteChannel rbc = Channels.newChannel(source)
            ) {
                buffer = createByteBuffer(bufferSize);

                while (true) {
                    int bytes = rbc.read(buffer);
                    if (bytes == -1) {
                        break;
                    }
                    if (buffer.remaining() == 0) {
                        buffer = resizeBuffer(buffer, buffer.capacity() * 3 / 2); // 50%
                    }
                }
            }
        }

        buffer.flip();
        return memSlice(buffer);
    }

    private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        ByteBuffer newBuffer = createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }

    public Mesh renderText(String text) {
        float scale = stbtt_ScaleForPixelHeight(info, getFontHeight());
        List<Float> positions = new ArrayList<>();
        List<Float> textureCoordinates = new ArrayList<>();
        float[] normals = new float[0];
        List<Integer> indices = new ArrayList<>();

        try (MemoryStack stack = stackPush()) {
            IntBuffer pCodePoint = stack.mallocInt(1);

            FloatBuffer x = stack.floats(0.0f);
            FloatBuffer y = stack.floats(0.0f);

            STBTTAlignedQuad q = STBTTAlignedQuad.mallocStack(stack);

            int lineStart = 0;

            float factorX = 1.0f / getContentScaleX();
            float factorY = 1.0f / getContentScaleY();

            float lineY = 0.0f;

            int counter = 0;

            for (int i = 0, to = text.length(); i < to; ) {
                i += getCP(text, to, i, pCodePoint);

                int cp = pCodePoint.get(0);
                if (cp == '\n') {
                    if (isLineBBEnabled()) {
                        glEnd();
                        renderLineBB(text, lineStart, i - 1, y.get(0), scale);
                        glBegin(GL_QUADS);
                    }

                    y.put(0, lineY = y.get(0) + (ascent - descent + lineGap) * scale);
                    x.put(0, 0.0f);

                    lineStart = i;
                    continue;
                } else if (cp < 32 || 354 <= cp) {
                    continue;
                }

                float cpX = x.get(0);
                stbtt_GetBakedQuad(cdata, BITMAP_W, BITMAP_H, cp - 32, x, y, q, true);
                x.put(0, scale(cpX, x.get(0), factorX));
                if (isKerningEnabled() && i < to) {
                    getCP(text, to, i, pCodePoint);
                    x.put(0, x.get(0) + stbtt_GetCodepointKernAdvance(info, cp, pCodePoint.get(0)) * scale);
                }

                float
                        x0 = scale(cpX, q.x0(), factorX),
                        x1 = scale(cpX, q.x1(), factorX),
                        y0 = scale(lineY, q.y0(), factorY),
                        y1 = scale(lineY, q.y1(), factorY);

                positions.add(x0);
                positions.add(y0);
                positions.add(0f);
                textureCoordinates.add(q.s0());
                textureCoordinates.add(q.t0());
                indices.add(counter*4);

                positions.add(x1);
                positions.add(y0);
                positions.add(0f);
                textureCoordinates.add(q.s1());
                textureCoordinates.add(q.t0());
                indices.add(counter*4+1);

                positions.add(x1);
                positions.add(y1);
                positions.add(0f);
                textureCoordinates.add(q.s1());
                textureCoordinates.add(q.t1());
                indices.add(counter*4+2);

                positions.add(x0);
                positions.add(y1);
                positions.add(0f);
                textureCoordinates.add(q.s0());
                textureCoordinates.add(q.t1());
                indices.add(counter*4+3);

                indices.add(counter*4);
                indices.add(counter*4+2);

    //                glTexCoord2f(q.s0(), q.t0());
    //                glVertex2f(x0, y0);
    //
    //                glTexCoord2f(q.s1(), q.t0());
    //                glVertex2f(x1, y0);
    //
    //                glTexCoord2f(q.s1(), q.t1());
    //                glVertex2f(x1, y1);
    //
    //                glTexCoord2f(q.s0(), q.t1());
    //                glVertex2f(x0, y1);
                counter++;
            }
            if (isLineBBEnabled()) {
                renderLineBB(text, lineStart, text.length(), lineY, scale);
            }
        }

        float[] pos = Utils.listToArray(positions);
        float[] tex = Utils.listToArray(textureCoordinates);
        int[] idx = indices.stream().mapToInt(i -> i).toArray();

        Mesh mesh = new Mesh(pos, tex, normals, idx);
        mesh.setMaterial(new Material(bitmapTexture));
        return mesh;
    }

    private float scale(float center, float offset, float factor) {
        return (offset - center) * factor + center;
    }

    private void renderLineBB(String text, int from, int to, float y, float scale) {
        glDisable(GL_TEXTURE_2D);
        glPolygonMode(GL_FRONT, GL_LINE);
        glColor3f(1.0f, 1.0f, 0.0f);

        float width = getStringWidth(info, text, from, to, getFontHeight());
        y -= descent * scale;

        glBegin(GL_QUADS);
        glVertex2f(0.0f, y);
        glVertex2f(width, y);
        glVertex2f(width, y - getFontHeight());
        glVertex2f(0.0f, y - getFontHeight());
        glEnd();

        glEnable(GL_TEXTURE_2D);
        glPolygonMode(GL_FRONT, GL_FILL);
        glColor3f(169f / 255f, 183f / 255f, 198f / 255f); // Text color
    }

    private float getStringWidth(STBTTFontinfo info, String text, int from, int to, int fontHeight) {
        int width = 0;

        try (MemoryStack stack = stackPush()) {
            IntBuffer pCodePoint       = stack.mallocInt(1);
            IntBuffer pAdvancedWidth   = stack.mallocInt(1);
            IntBuffer pLeftSideBearing = stack.mallocInt(1);

            int i = from;
            while (i < to) {
                i += getCP(text, to, i, pCodePoint);
                int cp = pCodePoint.get(0);

                stbtt_GetCodepointHMetrics(info, cp, pAdvancedWidth, pLeftSideBearing);
                width += pAdvancedWidth.get(0);

                if (isKerningEnabled() && i < to) {
                    getCP(text, to, i, pCodePoint);
                    width += stbtt_GetCodepointKernAdvance(info, cp, pCodePoint.get(0));
                }
            }
        }

        return width * stbtt_ScaleForPixelHeight(info, fontHeight);
    }

    private static int getCP(String text, int to, int i, IntBuffer cpOut) {
        char c1 = text.charAt(i);
        if (Character.isHighSurrogate(c1) && i + 1 < to) {
            char c2 = text.charAt(i + 1);
            if (Character.isLowSurrogate(c2)) {
                cpOut.put(0, Character.toCodePoint(c1, c2));
                return 2;
            }
        }
        cpOut.put(0, c1);
        return 1;
    }

    //todo: rework, old code
    protected void windowSizeChanged(long window, int width, int height){
        if (Platform.get() != Platform.MACOSX) {
            width /= contentScaleX;
            height /= contentScaleY;
        }

        this.ww = width;
        this.wh = height;

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0.0, width, height, 0.0, -1.0, 1.0);
        glMatrixMode(GL_MODELVIEW);

        setLineOffset(lineOffset);
    }

    public int getBitMapW() {
        return BITMAP_W;
    }

    public int getBitMapH() {
        return BITMAP_H;
    }

    public Texture getBitMapTexture() {
        return bitmapTexture;
    }
}
