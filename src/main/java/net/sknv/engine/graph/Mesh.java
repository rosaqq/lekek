package net.sknv.engine.graph;

import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.io.Serializable;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class Mesh implements Serializable {

    public final int vaoId;
    public final List<Integer> vboIdList;
    public final int vertexCount;
    //private Material material;
    private final int drawMode;
    private final Vector3f min, max;
    private final ArrayList<Vector3f> vertices;
    private String modelFile;

    public Mesh(float[] pos, float[] textCoords, float[] normals, int[] idx, int drawmode) {
        this.drawMode = drawmode;

        FloatBuffer posbuff = null;
        FloatBuffer textCoordsBuff = null;
        FloatBuffer vecNormalsBuffer = null;
        IntBuffer idxbuff = null;

        // Start BB code
        // -------------------------------------------------------------------------------------------------------------
        // generate values for bounding box
        min = new Vector3f(pos[0], pos[1], pos[2]);
        max = new Vector3f(pos[0], pos[1], pos[2]);
        for (int i = 0; i != pos.length; i += 3) {
            if (pos[i] < min.x) min.x = pos[i];
            if (pos[i + 1] < min.y) min.y = pos[i + 1];
            if (pos[i + 2] < min.z) min.z = pos[i + 2];

            if (pos[i] > max.x) max.x = pos[i];
            if (pos[i + 1] > max.y) max.y = pos[i + 1];
            if (pos[i + 2] > max.z) max.z = pos[i + 2];
        }

        // bounding box vertices
        vertices = new ArrayList<>(List.of(min, new Vector3f(min.x, min.y, max.z), new Vector3f(min.x, max.y, min.z), new Vector3f(max.x, min.y, min.z), new Vector3f(min.x, max.y, max.z), new Vector3f(max.x, min.y, max.z), new Vector3f(max.x, max.y, min.z), max));

        // end BB code
        // -------------------------------------------------------------------------------------------------------------

        try {
            vertexCount = idx.length;
            vboIdList = new ArrayList<>();

            /*
             * Generate VAO and get it's ID
             *
             * A VAO - Vertex Array Object - is an object in which we can store data about a 3D model.
             * VAOs have several slots where we can put our data - they are called "attribute lists"
             * Usually we store different sets of data in each list like, for example:
             * - vertex positions on list #0 (Positions VBO)
             * - texture coordinates on list #1 (Texture Coords VBO)
             * - vertex normals on list #2 (Vertex Normals VBO)
             *
             * Vertex normals are vectors assigned to each vertex, calculated using the normal vectors of the
             * surrounding triangles. These vectors will be used for lighting calculations.
             *
             * There is also a special set of data that is not stored in the attribute lists (Indices VBO).
             *
             * Data is stored as Vertex Buffer Objects - VBOs
             * VBOs are basically arrays of numbers with the data (like the example above).
             * Each VBO can be put into a separate attribute list in the VAO
             *
             * But how to access the data?
             * Each VAO has a unique ID, so when a VAO is stored in memory, we can access it at anytime with it's ID.
             *
             *
             * ╔════════════════════════╗
             * ║ Indices VBO explained: ║
             * ╚════════════════════════╝
             * Imagine drawing a quad:
             *
             * V1           V4
             *  ┌───────────┐
             *  │           │
             *  │           │
             *  └───────────┘
             * V2           V3
             *
             * 4 vertices but we need to draw 2 triangles, following OpenGL's counterclockwise logic:
             * V1,V2,V4 + V4,V2,V3
             *
             * Each triangle has 3 vertices and so our positions array will have 6*3 values (coords x,y,z) = 18 values!
             *
             * However, in a quad, the triangles share 2 vertices (V2 and V4), so we are passing the same vertex twice,
             * and doing that two times! Imagine the overhead for a bigger model!
             *
             * So we only put every vertex once on the Positions Array:
             * +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
             * |  0  |  1  |  2  |  3  |  4  |  5  |  6  |  7  |  8  |  9  | 10  | 11  | <- Indices of the Positions VBO
             * +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
             * | V1x | V1y | V1z | V2x | V2y | V2z | V3x | V3y | V3z | V4x | V4y | V4z | <- Values of the Positions VBO
             * +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
             * This Positions VBO has length 12. Without an index array it would be length 18 like mentioned before.
             *
             * And then use the Indices Array to tell OpenGL which vertex to render following OpenGL's order.
             * +---+---+---+---+---+---+
             * | 0 | 1 | 2 | 3 | 4 | 5 | <- Indices of the Indices VBO
             * +---+---+---+---+---+---+
             * | 0 | 1 | 3 | 3 | 1 | 2 | <- Values of the Indices VBO
             * +---+---+---+---+---+---+
             *
             * So basically it still draws 6 vertices but the Indices Array tells OpenGL in which position of the
             * Positions Array to look for the vertex coordinates.
             *
             * That's why below, when we put the VBO inside the VAO we need to give a "size" param, which specifies the
             * length of each element (vertex) in the Positions VBO.
             * Basically it tells OpenGL that:
             *   - vertex 0 starts at size*0 -> 3*0 = 0
             *   - vertex 3 starts at size*3 -> 3*3 = 9
             *
             * To draw vertex 0: coords start at PositionsArray[3*IndicesArray[0]] which is PositionsArray[0]
             * To draw vertex 1: coords start at PositionsArray[3*IndicesArray[1]] which is PositionsArray[3]
             * To draw vertex 2: coords start at PositionsArray[3*IndicesArray[2]] which is PositionsArray[9]
             * To draw vertex 3: coords start at PositionsArray[3*IndicesArray[3]] which is PositionsArray[9]
             * To draw vertex 4: coords start at PositionsArray[3*IndicesArray[4]] which is PositionsArray[3]
             * To draw vertex 5: coords start at PositionsArray[3*IndicesArray[5]] which is PositionsArray[6]
             *
             * This way we only have to save each vertex once. OpenGL then loops through the int array IndicesArray to
             * know which vertex to draw. Which is much more efficient that repeating thousands of vertices for complex
             * models (remember, repeating a vertex once means Positions Array increases size by 3).
             * If we use an Indices Array, repeating a vertex once only increases the Indices Array size by 1.
             *
             * ╔═══════════════════════════╗
             * ║ Back to creating the VAO: ║
             * ╚═══════════════════════════╝
             *
             * */
            vaoId = glGenVertexArrays();

            // Bind VAO so we can operate on it
            glBindVertexArray(vaoId);


            // Create the positions VBO, to put in the VAO's attribute list #0
            // ---------------------------------------------------------------------------------------------------------
            // Generate VBO and get ID. This first VBO will be explained in-depth. The rest not so much!
            int vboId = glGenBuffers();
            vboIdList.add(vboId);

            // create FloatBuffer
            posbuff = MemoryUtil.memAllocFloat(pos.length);
            // fill FloatBuffer and flip it for reading
            posbuff.put(pos).flip();

            // Bind VBO (specifying type)
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            // Store data in VBO
            glBufferData(GL_ARRAY_BUFFER, posbuff, GL_STATIC_DRAW);

            /*
             * Now to put the VBO in one of the VAO's attribute lists we use glVertexAttribPointer.
             * Params are as follows:
             *
             * - index: number of the VAO's attribute list
             * - size: length of each vertex (3 coords - x, y, z)
             * - type of data -> GL_FLOAT
             * - data is not normalized so false
             * - stride: distance between vertices (is there any other data between vertices? - no, so pass 0)
             * - pointer: offset at the beginning of the data
             * */
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            // Enable attribute list #0
            glEnableVertexAttribArray(0);

            // End of Positions VBO
            // ---------------------------------------------------------------------------------------------------------


            // Texture Coords VBO - attribute list #1
            // ---------------------------------------------------------------------------------------------------------

            if (textCoords != null) {
                vboId = glGenBuffers();
                vboIdList.add(vboId);
                textCoordsBuff = MemoryUtil.memAllocFloat(textCoords.length);
                textCoordsBuff.put(textCoords).flip();
                // With this, we change the bound VBO to this one (instead of the Positions VBO).
                glBindBuffer(GL_ARRAY_BUFFER, vboId);
                glBufferData(GL_ARRAY_BUFFER, textCoordsBuff, GL_STATIC_DRAW);
                // Put this VBO in attribute list #1
                glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
                // Enable attribute list #1
                glEnableVertexAttribArray(1);
            }

            // End of Texture Coords VBO
            // ---------------------------------------------------------------------------------------------------------

            // Vertex Normals VBO - attribute list #2
            // ---------------------------------------------------------------------------------------------------------

            if (normals != null) {
                vboId = glGenBuffers();
                vboIdList.add(vboId);
                vecNormalsBuffer = MemoryUtil.memAllocFloat(normals.length);
                vecNormalsBuffer.put(normals).flip();
                glBindBuffer(GL_ARRAY_BUFFER, vboId);
                glBufferData(GL_ARRAY_BUFFER, vecNormalsBuffer, GL_STATIC_DRAW);
                // Put this VBO in attribute list #2
                glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
                // Enable attribute list #2
                glEnableVertexAttribArray(2);
            }

            // End of Vertex Normals VBO
            // ---------------------------------------------------------------------------------------------------------

            // Unbind attribute list VBOs by binding the ID "0"
            glBindBuffer(GL_ARRAY_BUFFER, 0);

            // Index VBO - special case!
            // ---------------------------------------------------------------------------------------------------------

            vboId = glGenBuffers();
            vboIdList.add(vboId);
            idxbuff = MemoryUtil.memAllocInt(idx.length);
            idxbuff.put(idx).flip();
            // GL_ELEMENT_ARRAY_BUFFER is special. As long as we bind it while a VAO is bound, it gets attached to that
            // VAO's state. So there is no need to put it in an Attribute List with glVertexAttribPointer!
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, idxbuff, GL_STATIC_DRAW);

            // End of Index VBO
            // ---------------------------------------------------------------------------------------------------------

            // Unbind VAO by binding the ID "0"
            glBindVertexArray(0);

            // And now we have a VAO filled with the model data, with it's ID stored in vaoId!

        } finally {
            if (posbuff != null) {
                MemoryUtil.memFree(posbuff);
            }
            if (textCoordsBuff != null) {
                MemoryUtil.memFree(textCoordsBuff);
            }
            if (vecNormalsBuffer != null) {
                MemoryUtil.memFree(vecNormalsBuffer);
            }
            if (idxbuff != null) {
                MemoryUtil.memFree(idxbuff);
            }
        }
    }

    public Mesh(float[] pos, float[] textCoords, float[] normals, int[] idx) {
        this(pos, textCoords, normals, idx, GL_TRIANGLES);
    }

    public Mesh(float[] pos, int[] idx, int drawMode) {
        // constructor for stuff without textures and normals
        this(pos, null, null, idx, drawMode);
    }

//    public Material getMaterial() {
//        return material;
//    }

//    public void setMaterial(Material material) {
//        this.material = material;
//    }

    public int getVaoId() {
        return vaoId;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public String getModelFile() {
        return modelFile;
    }

    public void setModelFile(String fileName) {
        this.modelFile = fileName;
    }

    public void cleanUp() {
        glDisableVertexAttribArray(0);

        // Delete the VBOs
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        for (int vboId : vboIdList) {
            glDeleteBuffers(vboId);
        }

        // Delete the texture
        //material.getTexture().ifPresent(Texture::cleanup);

        // Delete the VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

    public void deleteBuffers() {
        glDisableVertexAttribArray(0);

        // Delete the VBOs
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        for (int vboId : vboIdList) {
            glDeleteBuffers(vboId);
        }

        // Delete the VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

    public Vector3f getMin() {
        return min;
    }

    public Vector3f getMax() {
        return max;
    }

    public ArrayList<Vector3f> getVertices() {
        return vertices;
    }

    @Override
    public String toString() {
        return "Mesh{" + "modelFile='" + modelFile + '\'' +
                //", material=" + material +
                '}';
    }

    public int getDrawMode() {
        return drawMode;
    }

//    @Override
//    public void render(ShaderProgram shaderProgram) {
//
//        material.getTexture().ifPresent( texture -> {
//            glActiveTexture(GL_TEXTURE0);
//            glBindTexture(GL_TEXTURE_2D, texture.getId());
//        });
//
//        //draw mesh
//        glBindVertexArray(getVaoId());
//
//        glDrawElements(getDrawMode(), getVertexCount(), GL_UNSIGNED_INT, 0);
//
//        //restore state
//        glBindVertexArray(0);
//        glBindTexture(GL_TEXTURE_2D, 0);
//    }
}