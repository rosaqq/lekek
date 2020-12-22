package net.sknv.game;

import net.sknv.engine.*;
import net.sknv.engine.graph.Camera;
import net.sknv.engine.graph.DirectionalLight;
import net.sknv.engine.physics.PhysicsEngine;
import net.sknv.engine.physics.colliders.OBB;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.Iterator;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class UltimateKekGame implements IGameLogic {

    private static final float MOUSE_SENSITIVITY = 0.4f;
    private static final float CAMERA_POS_STEP = 0.05f;

    private final Vector3f cameraPosInc;
    private final Vector2f cameraRotInc;

    private final Renderer renderer;
    private final Camera camera;

    private boolean menu = false;

    //light stuff
    private Vector3f ambientLight;
    private DirectionalLight directionalLight;
    private float lightAngle;

    private Scene scene;
    private Hud hud;

    //collisions stuff
    private PhysicsEngine physicsEngine;
    private OBB testBox;

    public GameItem movableItem;

    public UltimateKekGame() {
        renderer = new Renderer();
        physicsEngine = new PhysicsEngine();
        camera = new Camera();
        lightAngle = -90;

        cameraPosInc = new Vector3f(0, 0, 0);
        cameraRotInc = new Vector2f(0, 0);
    }

    @Override
    public void init(Window window, MouseInput mouseInput) throws Exception {
        renderer.init(window);
        setKeyCallbacks(window, mouseInput);

        scene = new Scene("SCENE1");

        initPhysicsEngine(scene);

        // Setup HUD
        hud = new Hud("+");

        //Setup Camera
        camera.setPosition(0.65f, 1.15f, 4.34f);

        //todo temp - figure this out
        movableItem = scene.getGameItems().get(0);
    }

    private void initPhysicsEngine(Scene scene) {
        List<GameItem> gameItems = scene.getGameItems();
        for (Iterator<GameItem> iterator = gameItems.iterator(); iterator.hasNext();) {
            GameItem gameItem = iterator.next();
            gameItem.getBoundingBox().transform();// converts bb coords from local to world <-- SOLVE THIS!!!
            try {
                physicsEngine.addGameItem(gameItem);
            } catch (Exception e){
                System.out.println("object colliding");;
            }
        }
    }

    @Override
    public void input(Window window, MouseInput mouseInput) {
        cameraPosInc.zero();
        if (window.isKeyPressed(GLFW_KEY_W)) cameraPosInc.z = -1;
        if (window.isKeyPressed(GLFW_KEY_S)) cameraPosInc.z = (cameraPosInc.z < 0 ? 0 : 1);
        if (window.isKeyPressed(GLFW_KEY_A)) cameraPosInc.x = -1;
        if (window.isKeyPressed(GLFW_KEY_D)) cameraPosInc.x = (cameraPosInc.x < 0 ? 0 : 1);
        if (window.isKeyPressed(GLFW_KEY_LEFT_SHIFT)) cameraPosInc.y = -1;
        if (window.isKeyPressed(GLFW_KEY_SPACE)) cameraPosInc.y = (cameraPosInc.y < 0 ? 0 : 1);

        if(cameraPosInc.length()!=0) cameraPosInc.normalize();

        if(renderer.getClicked()!=null) movableItem = renderer.getClicked();
        if (window.isKeyPressed(GLFW_KEY_UP)){
            if (window.isKeyPressed(GLFW_KEY_DOWN)) movableItem.getVelocity().z = 0f;
            else movableItem.getVelocity().z = .1f;
        } else if (window.isKeyPressed(GLFW_KEY_DOWN)) movableItem.getVelocity().z = -.1f;

        if (window.isKeyPressed(GLFW_KEY_LEFT)){
            if (window.isKeyPressed(GLFW_KEY_RIGHT)) movableItem.getVelocity().x = 0f;
            else movableItem.getVelocity().x = .1f;
        } else if (window.isKeyPressed(GLFW_KEY_RIGHT)) movableItem.getVelocity().x = -.1f;

        if (window.isKeyPressed(GLFW_KEY_RIGHT_SHIFT)){
            if (window.isKeyPressed(GLFW_KEY_RIGHT_CONTROL)) movableItem.getVelocity().x = 0f;
            else movableItem.getVelocity().x = .1f;
        } else if (window.isKeyPressed(GLFW_KEY_RIGHT_CONTROL)) movableItem.getVelocity().x = -.1f;

        if (window.isKeyPressed(GLFW_KEY_X)) movableItem.rotate(new Vector3f((float) (-Math.PI/200),0,0));
        if (window.isKeyPressed(GLFW_KEY_Y)) movableItem.rotate(new Vector3f(0,(float) (-Math.PI/200),0));
        if (window.isKeyPressed(GLFW_KEY_Z)) movableItem.rotate(new Vector3f(0,0,(float) (-Math.PI/200)));

        if (window.isKeyPressed(GLFW_KEY_K)) movableItem.setRot(0, 0, 0);

    }

    private void moveCamera(Window window, MouseInput mouseInput) {
        // moves camera pos
        camera.movePosition(cameraPosInc.x * CAMERA_POS_STEP, cameraPosInc.y * CAMERA_POS_STEP, cameraPosInc.z * CAMERA_POS_STEP);

        // rotates camera
        if (!menu && glfwGetWindowAttrib(window.getWindowHandle(), GLFW_FOCUSED) == 1) {
            Vector2f rotVec = mouseInput.getDisplVec();
            camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
        }
    }

    @Override
    public void update(Window window, MouseInput mouseInput, float interval) {
        physicsEngine.simulate(scene);
        moveCamera(window, mouseInput);
        hud.rotateCompass(camera.getRotation().y);
    }

    @Override
    public void render(Window window, MouseInput mouseInput) {
        hud.updateSize(window);
        renderer.render(window, mouseInput, camera, scene, hud);
    }

    @Override
    public void cleanup() {
        renderer.cleanup();
        for(GameItem item : scene.getGameItems()) item.getMesh().cleanUp();
        hud.cleanup();
    }

    /**
     * Use the body of this method to set the key callbacks
     * Use key callbacks for single press actions, like opening a menu with "P"
     *
     * For movement keys, where you just want to know if the key IS being pressed,
     * use window.isKeyPressed(key)
     * */
    private void setKeyCallbacks(Window window, MouseInput mouseInput) {
        window.setKeyCallback((windowHandle, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_P && action == GLFW_PRESS) {
                if(menu){
                    menu = false;
                    mouseInput.setDisabled();
                    glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
                } else {
                    menu = true;
                    mouseInput.setEnabled();
                    glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
                    glfwSetCursorPos(windowHandle, window.getCenter().x, window.getCenter().y);
                }
            }
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(windowHandle, true);
            }
        });
    }
}
