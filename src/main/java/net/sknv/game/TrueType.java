package net.sknv.game;

import net.sknv.engine.Utils;
import net.sknv.engine.graph.Material;
import net.sknv.engine.graph.Mesh;
import net.sknv.engine.graph.Texture;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.*;
import org.lwjgl.system.MemoryStack;

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

import static java.lang.Math.round;
import static org.lwjgl.BufferUtils.createByteBuffer;
import static org.lwjgl.glfw.GLFW.glfwGetMonitorContentScale;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
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

    private float scale;
    private int   lineOffset = 0;
    private float lineHeight = 0;

    private boolean kerningEnabled = false;//todo: stbtt_GetCodepointKernAdvance throws access violation sometimes(?) so its set to false while no fix
    private boolean lineBBEnabled = false;

    private int BITMAP_W = 512;//todo: find a way to calculate bitmap texture size
    private int BITMAP_H = 512;
    private Texture bitmapTexture;
    private STBTTPackedchar.Buffer myCharData;

    public TrueType(String inputFile){
        try {//todo: hardcode
            this.ttf = ioResourceToByteBuffer(inputFile);
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

        //set content scale
        try (MemoryStack s = stackPush()) {
            FloatBuffer px = s.mallocFloat(1);
            FloatBuffer py = s.mallocFloat(1);

            glfwGetMonitorContentScale(glfwGetPrimaryMonitor(), px, py);

            contentScaleX = px.get(0);
            contentScaleY = py.get(0);
        }
        scale = stbtt_ScaleForPixelHeight(info, getFontHeight());

        //BakeBitMapTexure
        BITMAP_W = round(BITMAP_W * contentScaleX);
        BITMAP_H = round(BITMAP_H * contentScaleY);

        myCharData = STBTTPackedchar.malloc(1000);

        STBTTPackRange.Buffer packRanges = STBTTPackRange.malloc(1);
        packRanges.put(STBTTPackRange.create().set(24, 0, null, 1000, myCharData, (byte) 0, (byte) 0)).flip();

        STBTTPackContext context = STBTTPackContext.malloc();
        ByteBuffer packedBitmap = BufferUtils.createByteBuffer(BITMAP_W * BITMAP_H);

        stbtt_PackBegin(context, packedBitmap,BITMAP_W, BITMAP_H,0,1);
        stbtt_PackFontRanges(context, ttf,0, packRanges);
        stbtt_PackEnd(context);

        bitmapTexture = new Texture(packedBitmap, BITMAP_W, BITMAP_H);
    }

    public STBTTFontinfo getInfo() {
        return info;
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
    private boolean isLineBBEnabled() {
        return lineBBEnabled;
    }
    private boolean isKerningEnabled() {
        return kerningEnabled;
    }

    public Mesh renderText(String text) {
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
                }

                float cpX = x.get(0);
                stbtt_GetPackedQuad(myCharData, BITMAP_W, BITMAP_H, cp, x, y, q, true);
                x.put(0, scale(cpX, x.get(0), factorX));
                if (isKerningEnabled() && i < to) {
                    getCP(text, to, i, pCodePoint);
                    System.out.println(cp);
                    System.out.println(pCodePoint.get(0));
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

    public static ByteBuffer ioResourceToByteBuffer(String resource) throws IOException {
        ByteBuffer buffer;

        Path path = Paths.get(resource);
        if (Files.isReadable(path)) {
            try (SeekableByteChannel fc = Files.newByteChannel(path)) {
                buffer = createByteBuffer((int)fc.size() + 1);
                while (fc.read(buffer) != -1);
            }
        } else {
            try (
                    InputStream source = TrueType.class.getClassLoader().getResourceAsStream(resource);
                    ReadableByteChannel rbc = Channels.newChannel(source)
            ) {
                buffer = createByteBuffer(1024);

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

    private float scale(float center, float offset, float factor) {
        return (offset - center) * factor + center;
    }

    private void renderLineBB(String text, int from, int to, float y, float scale) {//todo: old opengl
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
