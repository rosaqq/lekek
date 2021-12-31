package net.sknv.engine.graph;

import net.sknv.engine.physics.colliders.BoundingBox;
import net.sknv.engine.physics.colliders.OBB;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.opengl.GL11.GL_LINES;

public class MeshUtils {
	public static Mesh generateCube(WebColor color, int drawmode) {
		return generateCube(color, 1, 1, drawmode);
	}

	public static Mesh generateCube(WebColor color, float alpha, float reflectance, int drawmode) {
		return generateCube(new Material(color, alpha, reflectance), drawmode);
	}

	public static Mesh generateCube(Vector4f color, float reflectance, int drawmode) {
		return generateCube(new Material(color, reflectance), drawmode);
	}

	public static Mesh generateCube(Material material, int drawMode) {
		float[] posArr = new float[]{1, -1, -1, 1, -1, 1, -1, -1, 1, -1, -1, -1, 1, 1, -1, 1, 1, 1, -1, 1, 1, -1, 1, -1, 1, -1, -1, 1, -1, -1, 1, -1, 1, 1, -1, 1, -1, -1, -1, -1, -1, -1, 1, 1, -1, 1, 1, -1, -1, -1, 1, -1, -1, 1, 1, 1, 1, 1, 1, 1, -1, 1, 1, -1, 1, 1, -1, 1, -1, -1, 1, -1};
		float[] normalsArr = new float[]{0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f};
		int[] idxArr = new int[]{10, 16, 12, 23, 21, 19, 14, 18, 11, 5, 20, 17, 2, 6, 22, 0, 3, 7, 8, 10, 12, 15, 23, 19, 9, 14, 11, 1, 5, 17, 13, 2, 22, 4, 0, 7};

		float[] texCoordsArr = new float[1];
		Mesh mesh = new Mesh(posArr, normalsArr, texCoordsArr, idxArr, drawMode);
		//mesh.setMaterial(material);
		return mesh;
	}

	public static Mesh generateLine(Vector3f start, Vector3f end) {
		// Create Vertex Positions Array
		float[] posArray = new float[]{start.x, start.y, start.z, end.x, end.y, end.z};
		// Create Indices Array
		int[] idxArray = new int[]{0, 1};
		Mesh mesh = new Mesh(posArray, idxArray, GL_LINES);

		return mesh;
	}

	public static Mesh generateAABB(WebColor color, BoundingBox bb) {
		Vector3f min = bb.getMin().getPosition();
		Vector3f max = bb.getMax().getPosition();

		// Create Positions Array
		float[] posArray = new float[]{min.x, min.y, min.z, max.x, min.y, min.z, min.x, max.y, min.z, min.x, min.y, max.z,

				min.x, max.y, max.z, max.x, min.y, max.z, max.x, max.y, min.z, max.x, max.y, max.z,};

		// Create Indices Array
		int[] idxArray = new int[]{0, 1, 0, 2, 0, 3,

				1, 5, 1, 6,

				2, 4, 2, 6,

				3, 4, 3, 5,

				4, 7, 5, 7, 6, 7,};

		Mesh mesh = new Mesh(posArray, idxArray, GL_LINES);
		//mesh.setMaterial(new Material(color, 1, 1));
		return mesh;
	}

	public static Mesh generateOBB(WebColor color, BoundingBox bb) {
		Vector3f center = ((OBB) bb).getCenter();

		Vector3f x = ((OBB) bb).getX();
		Vector3f y = ((OBB) bb).getY();
		Vector3f z = ((OBB) bb).getZ();

		// Create Positions Array
		float[] posArray = new float[]{
				//Axis
				center.x, center.y, center.z,

				center.x + x.x, center.y + x.y, center.z + x.z, center.x + y.x, center.y + y.y, center.z + y.z, center.x + z.x, center.y + z.y, center.z + z.z,

				//exact Box
				center.x + x.x + y.x + z.x, center.y + x.y + y.y + z.y, center.z + x.z + y.z + z.z, //max

				center.x - x.x + y.x + z.x, center.y - x.y + y.y + z.y, center.z - x.z + y.z + z.z, center.x + x.x - y.x + z.x, center.y + x.y - y.y + z.y, center.z + x.z - y.z + z.z, center.x + x.x + y.x - z.x, center.y + x.y + y.y - z.y, center.z + x.z + y.z - z.z,

				center.x - x.x - y.x - z.x, center.y - x.y - y.y - z.y, center.z - x.z - y.z - z.z, //min

				center.x + x.x - y.x - z.x, center.y + x.y - y.y - z.y, center.z + x.z - y.z - z.z, center.x - x.x + y.x - z.x, center.y - x.y + y.y - z.y, center.z - x.z + y.z - z.z, center.x - x.x - y.x + z.x, center.y - x.y - y.y + z.y, center.z - x.z - y.z + z.z,};

		// Create Indices Array
		int[] idxArray = new int[]{0, 1, 0, 2, 0, 3,

				4, 5, 4, 6, 4, 7,

				8, 9, 8, 10, 8, 11,

				5, 10, 5, 11, 6, 11, 6, 9, 7, 9, 7, 10,};


		Mesh mesh = new Mesh(posArray, idxArray, GL_LINES);
		//mesh.setMaterial(new Material(color, 1, 1));
		return mesh;
	}


}
