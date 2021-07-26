package net.sknv.game;

import net.sknv.engine.IGameLogic;
import net.sknv.engine.MouseInput;
import net.sknv.engine.Scene;
import net.sknv.engine.Window;
import net.sknv.engine.entities.AbstractGameItem;
import net.sknv.engine.entities.Collider;
import net.sknv.engine.entities.HudElement;
import net.sknv.engine.entities.Phantom;
import net.sknv.engine.graph.*;
import net.sknv.engine.physics.PhysicsEngine;
import net.sknv.engine.physics.colliders.OBB;
import org.joml.*;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.round;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glViewport;

public class UltimateKekGame implements IGameLogic {

    private static final float FOV = (float) Math.toRadians(60.0f);
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 1000.f;

    private static Matrix4f projectionMatrix, viewMatrix, ortho;
    private ArrayList<RayCast> rayCasts = new ArrayList<>();

    private static final float MOUSE_SENSITIVITY = 0.003f;
    private static final float CAMERA_POS_STEP = 0.03f;

    private final Vector3f cameraPosInc;
    private final Vector3f cameraRotInc;

    private final Renderer renderer;
    private final Camera camera;

    private boolean menu = false;
    private boolean usingTerminal = false;

    private Scene scene;
    private Hud hud;

    //font stuff
    private TrueType font;

    //collisions stuff
    private PhysicsEngine physicsEngine;
    public Collider selectedItem;

    public UltimateKekGame() {
        renderer = new Renderer();
        camera = new Camera(new Vector3f(), new Quaternionf());
        cameraPosInc = new Vector3f();
        cameraRotInc = new Vector3f();
    }

    @Override
    public void init(Window window, MouseInput mouseInput) throws Exception {
        //todo: spaghet
        font = new TrueType(window.getWindowHandle());
        int BITMAP_W = round(168 * font.getContentScaleX());
        int BITMAP_H = round(168 * font.getContentScaleY());

        float zPos = 0;
        Mesh myMesh = new Mesh(new float[]{0,0,zPos,0,BITMAP_H,zPos,BITMAP_W,BITMAP_H,zPos,BITMAP_W,0,zPos}, new float[]{0,0,0,1,1,1,1,0},new float[0],new int[]{0,1,2,0,2,3}, GL_TRIANGLES);
        //myMesh.setMaterial(new Material(new Texture("src/main/resources/textures/font_texture.png")));
        myMesh.setMaterial(new Material(new Texture(font)));
        myMesh.getMaterial().setAmbientColor(new Vector4f(1, 1, 1, 1));

        HudElement myElement = new HudElement(myMesh);
        //

        renderer.init();
        setKeyCallbacks(window, mouseInput);
        //fontstuff
        glfwSetWindowSizeCallback(window.getWindowHandle(), font::windowSizeChanged);

        initScene("default");

        Mesh line = MeshUtils.generateLine(WebColor.Red, new Vector3f(0,0,0), new Vector3f(10,10,0));
        //scene.addGameItem(new Phantom(line));

        initPhysicsEngine();

        // Setup HUD
        hud = new Hud("+");
        //todo:spaghetti
        hud.addElement(myElement);

        //Setup Camera
        camera.setPosition(0.65f, 1.15f, 4.34f);
    }

    public void initScene(String scene) {
        try {

            if (scene.equals("default")) this.scene = new Scene();
            else {
                FileInputStream file = new FileInputStream("src/main/resources/scenes/" + scene + ".ser");
                this.scene = (Scene) new ObjectInputStream(file).readObject();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initPhysicsEngine() {
        physicsEngine = new PhysicsEngine();
        List<AbstractGameItem> gameItems = scene.getGameItems();
        for (AbstractGameItem gameItem : gameItems) {
            //todo: temporary, GameItem empty constructor should deprecate and bb should be initialized on a constructor with all params,
            // unless it is decided to not deprecate the empty constructor to support other types of gameItems (Huds, lines, stuff)
            // in that case class inheritance should be pondered

            // todo: temp if to prevent HudElements from getting called in the Physics Engine
            if (gameItem instanceof Collider) {
                Collider collider = (Collider) gameItem;
                collider.setBoundingBox(new OBB(collider));
                try {
                    physicsEngine.addGameItem(collider);
                } catch (Exception e) {
                    System.out.println("object colliding");
                }
            }
        }
    }

    @Override
    public void input(Window window, MouseInput mouseInput) {

        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }

        //update matrices
        projectionMatrix = Transformation.getProjectionMatrix(FOV, window.getWidth(), window.getHeight(), Z_NEAR, Z_FAR);
        viewMatrix = Transformation.getViewMatrix(camera);
        ortho = Transformation.getOrthoProjectionMatrix(0, window.getWidth(), window.getHeight(), 0);

        cameraPosInc.zero();
        hud.updateSize(window);

        if (!usingTerminal) {
            if (window.isKeyPressed(GLFW_KEY_W)) cameraPosInc.z = -1;
            if (window.isKeyPressed(GLFW_KEY_S)) cameraPosInc.z = (cameraPosInc.z < 0 ? 0 : 1);
            if (window.isKeyPressed(GLFW_KEY_A)) cameraPosInc.x = -1;
            if (window.isKeyPressed(GLFW_KEY_D)) cameraPosInc.x = (cameraPosInc.x < 0 ? 0 : 1);
            if (window.isKeyPressed(GLFW_KEY_LEFT_SHIFT)) cameraPosInc.y = -1;
            if (window.isKeyPressed(GLFW_KEY_SPACE)) cameraPosInc.y = (cameraPosInc.y < 0 ? 0 : 1);
            if (window.isKeyPressed(GLFW_KEY_K)) System.out.println(scene.getGameItems());

            if (cameraPosInc.length() != 0) cameraPosInc.normalize();

            if(selectedItem!=null) {
                if (window.isKeyPressed(GLFW_KEY_UP)) {
                    if (window.isKeyPressed(GLFW_KEY_DOWN)) selectedItem.getVelocity().z = 0f;
                    else selectedItem.getVelocity().z = -.1f;
                } else if (window.isKeyPressed(GLFW_KEY_DOWN)) selectedItem.getVelocity().z = .1f;

                if (window.isKeyPressed(GLFW_KEY_LEFT)) {
                    if (window.isKeyPressed(GLFW_KEY_RIGHT)) selectedItem.getVelocity().x = 0f;
                    else selectedItem.getVelocity().x = -.1f;
                } else if (window.isKeyPressed(GLFW_KEY_RIGHT)) selectedItem.getVelocity().x = .1f;

                if (window.isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
                    if (window.isKeyPressed(GLFW_KEY_RIGHT_CONTROL)) selectedItem.getVelocity().y = 0f;
                    else selectedItem.getVelocity().y = .1f;
                } else if (window.isKeyPressed(GLFW_KEY_RIGHT_CONTROL)) selectedItem.getVelocity().y = -.1f;

                if (window.isKeyPressed(GLFW_KEY_X))
                    selectedItem.rotateEuclidean(new Vector3f((float) (-Math.PI / 200), 0, 0));
                if (window.isKeyPressed(GLFW_KEY_Y))
                    selectedItem.rotateEuclidean(new Vector3f(0, (float) (-Math.PI / 200), 0));
                if (window.isKeyPressed(GLFW_KEY_Z))
                    selectedItem.rotateEuclidean(new Vector3f(0, 0, (float) (-Math.PI / 200)));
                if (window.isKeyPressed(GLFW_KEY_K)) selectedItem.setRotationEuclidean(new Vector3f());
                if (window.isKeyPressed(GLFW_KEY_J)) selectedItem.rotateWorldEuclidean(new Vector3f(0, (float)Math.PI/200, 0));
            }
        }

        //ray casting
        Vector3f worldRay = mouseInput.getWorldRay(window, projectionMatrix, viewMatrix);
        Vector3f cameraPos = camera.getPosition();
        RayCast ray = new RayCast(new Vector3f(cameraPos), new Vector3f(worldRay.x, worldRay.y, worldRay.z));

        if(mouseInput.isRightClicked()) {
            Vector3f end = new Vector3f();
            ray.origin.add(ray.direction.mul(20f), end);
            scene.addGameItem(new Phantom(MeshUtils.generateLine(WebColor.Yellow, new Vector3f(ray.origin), new Vector3f(end))));
        }

        ArrayList<Collider> clickedItems = new ArrayList<>();
        for (AbstractGameItem gameItem : scene.getGameItems()) {
            if (gameItem instanceof Collider) {
                // todo: spaghet
                Collider collider = (Collider) gameItem;
                if(mouseInput.isLeftClicked() && ray.intersectsItem(collider)) clickedItems.add(collider);
            }
        }

        if(selectedItem != null) selectedItem.drawBB(WebColor.Green);

        if(!clickedItems.isEmpty()) {
            float d = cameraPos.distance(clickedItems.get(0).getPosition());
            for (Collider item : clickedItems) {
                if (cameraPos.distance(item.getPosition()) <= d){
                    d = cameraPos.distance(item.getPosition());
                    selectedItem = item;
                }
                item.drawBB(WebColor.Yellow);
            }
            selectedItem.drawBB(WebColor.Red);
        }

    }

    private void moveCamera(Window window, MouseInput mouseInput) {

        Vector3f prevPos = new Vector3f(camera.getPosition());
        // moves camera pos
        if(!(menu || usingTerminal)) camera.movePosition(cameraPosInc.x * CAMERA_POS_STEP, cameraPosInc.y * CAMERA_POS_STEP, cameraPosInc.z * CAMERA_POS_STEP);

        // Check if there has been a collision. If true, set the y position to
        // the maximum height
        /*
        float height = scene.getTerrain().getHeight(camera.getPosition());
        if ( camera.getPosition().y <= height )  {
            camera.setPosition(prevPos.x, prevPos.y, prevPos.z);
        }
         */

        // rotates camera
        if (!(menu || usingTerminal) && glfwGetWindowAttrib(window.getWindowHandle(), GLFW_FOCUSED) == 1) {
            Vector2f rotVec = mouseInput.getDisplVec();
            cameraRotInc.set(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
            camera.moveRotation(cameraRotInc);
        }
    }

    @Override
    public void update(Window window, MouseInput mouseInput, float interval) {
        physicsEngine.simulate(scene);
        moveCamera(window, mouseInput);
        hud.rotateCompass(camera.getRotation().y);
    }

    @Override
    public void render() {
        renderer.render(projectionMatrix, viewMatrix, ortho, scene, hud);

        //todo: super spaghet, hard code
            //glClear(GL_COLOR_BUFFER_BIT);

            float scaleFactor = 1.0f + font.getScale() * 0.25f;

            // Zoom
            //glScalef(scaleFactor, scaleFactor, 1f);
            // Scroll
            //glTranslatef(4.0f, font.getFontHeight() * 0.5f + 4.0f - font.getLineOffset() * font.getFontHeight(), 0f);

            //font.renderText(cdata, BITMAP_W, BITMAP_H);

        //cdata.free();//on cleanup!
    }

    @Override
    public void cleanup() {
        renderer.cleanup();
        for(AbstractGameItem item : scene.getGameItems()) {
            // todo: temp fix for retro-compat. Update AbsGameItem to include cleanup methods or find other good solution.
            if (item instanceof Phantom) ((Phantom)item).getMesh().cleanUp();
        }
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
            if (usingTerminal && (action == GLFW_PRESS || action == GLFW_REPEAT)){ //using hud terminal
                if(key>=48 && key<=90){
                    hud.getTerminal().addText(String.valueOf((char)Character.toLowerCase(key)));
                } else if (key == 32) hud.getTerminal().addText(" ");
                else if (key == 257) {
                    processTerminal(hud.getTerminal().enter());
                    closeHudTerminal(mouseInput, windowHandle);
                }
                else if (key == 259) hud.getTerminal().backspace();
                else if (key == 265) hud.getTerminal().previous();
                else if (key == 264) hud.getTerminal().recent();
            }

            if (key == GLFW_KEY_P && action == GLFW_PRESS && !usingTerminal) {
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
                if(usingTerminal){ //closing hud terminal
                    closeHudTerminal(mouseInput, windowHandle);
                } else glfwSetWindowShouldClose(windowHandle, true); //closing game
            }
            if (!usingTerminal && key == GLFW_KEY_T && action == GLFW_PRESS){ //opening hud terminal
                openHudTerminal(window, mouseInput, windowHandle);
            }
        });
    }

    private void openHudTerminal(Window window, MouseInput mouseInput, long windowHandle) {
        usingTerminal = true;
        mouseInput.setEnabled();
        glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        glfwSetCursorPos(windowHandle, window.getCenter().x, window.getCenter().y);
        hud.showTerminal();
    }

    private void closeHudTerminal(MouseInput mouseInput, long windowHandle) {
        usingTerminal = false;
        mouseInput.setDisabled();
        glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        hud.hideTerminal();
    }

    public void processTerminal(String input) {
        if (input==null) return;

        String[] in = input.split(" ");

        switch (in[0]){
            case "savescene":
                String sceneName;
                if(in.length>1) sceneName = in[1]; else sceneName = "unnamed";
                getScene().save(sceneName);
                break;
            case "loadscene":
                if(in.length>1) sceneName = in[1]; else return;
                System.out.println("loading scene - " + sceneName);
                initScene(sceneName);
                initPhysicsEngine();
                System.out.println("scene loaded");
                break;
            case "clearitems":
                scene.removeAllItems();
                break;
            case "removeitem":
                scene.removeItem(selectedItem);
                break;
            case "rotateitem":
                if(in.length==4) selectedItem.rotateEuclidean(new Vector3f(Float.parseFloat(in[1]), Float.parseFloat(in[2]), Float.parseFloat(in[3])));
                break;
            case "additem":
                try {
                    String model = in[1];
                    Mesh mesh = OBJLoader.loadMesh("/models/" + model + ".obj");
                    Texture texture = new Texture("src/main/resources/textures/lebloq.png");
                    Material material = new Material(texture, 1f);
                    mesh.setMaterial(material);
                    Collider newItem = new Collider(mesh);
                    scene.addGameItem(newItem);

                    if(in.length==5) newItem.translate(new Vector3f(Float.parseFloat(in[2]), Float.parseFloat(in[3]), Float.parseFloat(in[4])));

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "addcubes":
                try {
                    int side = Integer.parseInt(in[1]);
                    float scale;
                    if (in.length > 2) {
                        System.out.println("scale: " + in[2]);
                        scale = Float.parseFloat(in[2]);
                    }
                    else scale = 1;

                    Mesh mesh = OBJLoader.loadMesh("/models/cube.obj");
                    Texture texture = new Texture("src/main/resources/textures/lebloq.png");
                    Material material = new Material(texture, 1f);
                    mesh.setMaterial(material);

                    int offset = side/2;
                    for (int i=0;i<side;i++) {
                        for (int j=0; j<side; j++) {
                            Collider newItem = new Collider(mesh);
                            scene.addGameItem(newItem);
                            newItem.setScale(scale);
                            newItem.setPosition(2*scale*(i-offset), 0, 2*scale*(j-offset));
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "quit":
                System.exit(0);
                break;
            default:
                break;
        }
    }

    public Scene getScene(){
        return scene;
    }

    public Hud getHud(){
        return hud;
    }

    private void setScene(Scene scene) {
        this.scene = scene;
    }
}
