package net.sknv.engine.graph;

import net.sknv.engine.entities.AbstractGameItem;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Transformation {
    /**
     * The Transformation class contains the methods that provide all the necessary matrices to
     * perform space transformations, changing the coordinates from one coordinate system to another.
     *
     * ╔══════════════════════════════════════════════════════════════════════════════════╗
     * ║ TL;DR for Matrices                                                               ║
     * ╠══════════════════════════════════════════════════════════════════════════════════╣
     * ║ ModelMatrix      -> Turns local Mesh coords into global World coords             ║
     * ║ ViewMatrix       -> Turns World coords to a World with the Camera as it's origin ║
     * ║ ProjectionMatrix -> Turns World based on Camera origin into frustum clip space   ║
     * ╚══════════════════════════════════════════════════════════════════════════════════╝
     *
     * There are 5 different coordinate systems:
     *
     * - Local Space (or Object Space):
     *      This is the object's self coordinate system and it is composed by local coordinates that are
     *      relative to the object's local origin (it's center). You can think of a object's local space as if it were
     *      alone in the world with its local origin overlapping the world's origin. That way the local space coordinates
     *      would define the positions of the object's vertices relative to its origin and the sum of the object's
     *      local coordinates compose its {@link Mesh}.
     *
     * - World Space:
     *      The world space is composed by world coordinates and are the ones we are most used to, these coordinates are
     *      relative to some global origin that is the world space origin. The world comes together by transforming all
     *      of it's objects local coordinates into world coordinates, these coordinates define then the positions of all
     *      the object's mesh vertices in the world.
     *
     *      Local coordinates can be transformed to world coordinates, "placing" the object in the world space:
     *      [Local Space] -> ModelMatrix -> [World Space]
     *
     * - View Space (or Eye Space):
     *      This space is the result of transforming world coordinates to coordinates that are in front of the camera's point of view,
     *      making the origin of this space the camera's position and aligning the xyz axis to the camera orientation. The result
     *      are the coordinates of objects as seen from the camera's position and orientation.
     *
     *      World coordinates can be transformed to view space coordinates, making the objects as seen from the camera's point of view:
     *      [World Space] -> ViewMatrix -> [World Space]
     *
     * - Clip Space:
     *      This space is the result of scaling the xyz axis into a frustum shape clipping all the view space coordinates out of a given coordinate set (frustum).
     *      The projection matrix is responsible for defining that coordinate set simulating a viewing box. Each coordinate inside this frustum will end up on the screen.
     *
     *
     *      After the vertices coordinates are transformed to clip space a final operation called perspective division is performed
     *      where we divide the x, y and z components of the position vectors by the vector's homogeneous w component.
     *      Perspective division is what transforms the 4D clip space coordinates to 3D normalized device coordinates, this is
     *      what gives perspective, changing the homogenous clip space to normalised device space.
     *
     *      View space coordinates can be transformed to clip space coordinates:
     *      [View Space] -> ProjectionMatrix -> [Homogenous Clip Space]
     *      View-space to clip-space coordinates can add perspective if using perspective projection.
     *
     * - Screen Space (or Viewport Space):
     *      The resulting coordinates are finally mapped to screen coordinates (using the settings of glViewport) and turned into fragments.
     *      The viewport transformation is a scale and translation that maps the -1 to 1 normalized device-coordinate cube into window coordinates range defined by glViewport.
     *      This transformation applies not only to x and y values, but also to the z value. The viewport transformation maps normalized device-coordinate z values
     *      to the range of acceptable depth buffer values.
     *
     *      Transformation is performed in shader (i think)
     *
     * https://learnopengl.com/Getting-started/Coordinate-Systems
     * https://antongerdelan.net/opengl/raycasting.html
     */

    private static final Matrix4f projectionMatrix = new Matrix4f();
    private static final Matrix4f viewMatrix = new Matrix4f();
    private static final Matrix4f modelViewMatrix = new Matrix4f();
    private static final Matrix4f orthogonalMatrix = new Matrix4f();

    public static Matrix4f getProjectionMatrix(float fov, float width, float height, float zNear, float zFar) {
        float aspectRatio = width / height;
        projectionMatrix.identity();
        projectionMatrix.perspective(fov, aspectRatio, zNear, zFar);
        return projectionMatrix;
    }

    public static Matrix4f getViewMatrix(Camera camera) {
        Vector3f cameraPos = camera.getPosition();
        Vector3f rot = camera.getRotation();

        viewMatrix.identity();
        //must rotate first so camera rotates over it's position
        viewMatrix.rotate(rot.x, new Vector3f(1, 0, 0))
                .rotate(rot.y, new Vector3f(0, 1, 0));
        //then do translation
        viewMatrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        return viewMatrix;
    }

    public static Matrix4f getOrthoProjectionMatrix(float left, float right, float bottom, float top) {
        orthogonalMatrix.identity();
        orthogonalMatrix.setOrtho2D(left, right, bottom, top);
        return orthogonalMatrix;
    }


    public static Matrix4f getModelViewMatrix(AbstractGameItem item, Matrix4f viewMatrix) {// modelViewMatrix = modelMatrix * viewMatrix
        //must create copy
        Matrix4f viewCurr = new Matrix4f(viewMatrix);
        //because this changes the values of viewCurr and since there is only one viewMatrix for all the items,
        //the values would accumulate every time this method ran
        return viewCurr.mul(getModelMatrix(item));
    }

    public static Matrix4f getOrtoProjModelMatrix(AbstractGameItem gameItem, Matrix4f orthoMatrix) {
        Matrix4f modelMatrix = getModelMatrix(gameItem);
        Matrix4f orthoMatrixCurr = new Matrix4f(orthoMatrix);
        orthoMatrixCurr.mul(modelMatrix);
        return orthoMatrixCurr;
    }

    public static Matrix4f getModelMatrix(AbstractGameItem gameItem){
        //Vector3f rotation = gameItem.getRotation();

        modelViewMatrix.identity().translate(gameItem.getPosition()).
                rotate(gameItem.getRotation()).
                //rotateX(rotation.x).
                //rotateY(rotation.y).
                //rotateZ(rotation.z).
                scale(gameItem.getScale());
        return modelViewMatrix;
    }

}
