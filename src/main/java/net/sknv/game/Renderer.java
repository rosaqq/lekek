package net.sknv.game;

import net.sknv.engine.IHud;
import net.sknv.engine.Scene;
import net.sknv.engine.SkyBox;
import net.sknv.engine.Utils;
import net.sknv.engine.entities.AbstractGameItem;
import net.sknv.engine.entities.Collider;
import net.sknv.engine.entities.HudElement;
import net.sknv.engine.graph.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class Renderer {

    private ShaderProgram shaderProgram, hudShaderProgram, skyBoxShaderProgram;

    private static final int MAX_POINT_LIGHTS = 5;
    private static final int MAX_SPOT_LIGHTS = 5;
    private float specularPower;

    public Renderer() {
        specularPower = 10f;
    }

    public void init() throws Exception {
        setupSceneShader();
        setupHudShader();
        setupSkyBoxShader();
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    private void setupSceneShader() throws Exception {
        //create shader
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(Utils.loadResource("/shaders/vertex.glsl"));
        shaderProgram.createFragmentShader(Utils.loadResource("/shaders/fragment.glsl"));
        shaderProgram.link();

        //create uniforms for world and projection matrices
        shaderProgram.createUniform("projectionMatrix");
        shaderProgram.createUniform("modelViewMatrix");
        shaderProgram.createUniform("texture_sampler");

        //material uniform
        shaderProgram.createMaterialUniform("material");

        //light uniforms
        shaderProgram.createUniform("specularPower");
        shaderProgram.createUniform("ambientLight");
        shaderProgram.createPointLightListUniform("pointLights", MAX_POINT_LIGHTS);
        shaderProgram.createSpotLightListUniform("spotLights", MAX_SPOT_LIGHTS);
        shaderProgram.createDirectionalLightUniform("directionalLight");
    }

    private void setupHudShader() throws Exception {
        hudShaderProgram = new ShaderProgram();
        hudShaderProgram.createVertexShader(Utils.loadResource("/shaders/hud_vertex.glsl"));
        hudShaderProgram.createFragmentShader(Utils.loadResource("/shaders/hud_fragment.glsl"));
        hudShaderProgram.link();

        // Create uniforms for Ortographic-model projection matrix and base colour
        hudShaderProgram.createUniform("projModelMatrix");
        hudShaderProgram.createUniform("colour");
        hudShaderProgram.createUniform("hasTexture");
    }

    private void setupSkyBoxShader() throws Exception {
        skyBoxShaderProgram = new ShaderProgram();
        skyBoxShaderProgram.createVertexShader(Utils.loadResource("/shaders/sb_vertex.glsl"));
        skyBoxShaderProgram.createFragmentShader(Utils.loadResource("/shaders/sb_fragment.glsl"));
        skyBoxShaderProgram.link();

        skyBoxShaderProgram.createUniform("projectionMatrix");
        skyBoxShaderProgram.createUniform("modelViewMatrix");
        skyBoxShaderProgram.createUniform("texture_sampler");
        skyBoxShaderProgram.createUniform("ambientLight");
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void render(Matrix4f projectionMatrix, Matrix4f viewMatrix, Matrix4f ortho, Scene scene, IHud hud) {
        // todo: idea is implement single loop based on IRenderable
        clear();
        renderScene(projectionMatrix, viewMatrix, scene);
        renderSkyBox(projectionMatrix, viewMatrix, scene);
        renderHud(ortho, hud);
    }

    private void renderScene(Matrix4f projectionMatrix, Matrix4f viewMatrix, Scene scene) {

        Vector3f ambientLight = scene.getSceneLight().getAmbientLight();
        DirectionalLight directionalLight = scene.getSceneLight().getDirectionalLight();

        shaderProgram.bind();
        shaderProgram.setUniform("projectionMatrix", projectionMatrix);
        shaderProgram.setUniform("texture_sampler", 0);

        //update light uniforms
        renderLights(viewMatrix, ambientLight, directionalLight);

        //render each game item
        for (AbstractGameItem gameItem : scene.getGameItems()) {

            Matrix4f transformationResult = Transformation.getModelViewMatrix(gameItem, viewMatrix);
            shaderProgram.setUniform("modelViewMatrix", transformationResult);
            shaderProgram.setUniform("material", gameItem.getMesh().getMaterial());

            gameItem.render(shaderProgram);

            //todo spaghet
            if (gameItem instanceof Collider && (((Collider) gameItem).getShowBB()!=null)) {
                Mesh aabbMesh = MeshUtils.generateAABB(((Collider) gameItem).getShowBB(), ((Collider) gameItem).getBoundingBox());
                Mesh obbMesh = MeshUtils.generateOBB(((Collider) gameItem).getShowBB(), ((Collider) gameItem).getBoundingBox());

                shaderProgram.setUniform("modelViewMatrix", viewMatrix);

                //draw meshes
                shaderProgram.setUniform("material", aabbMesh.getMaterial());
                glBindVertexArray(aabbMesh.getVaoId());
                glDrawElements(GL_LINES, aabbMesh.getVertexCount(), GL_UNSIGNED_INT, 0);

                shaderProgram.setUniform("material", obbMesh.getMaterial());
                glBindVertexArray(obbMesh.getVaoId());
                glDrawElements(GL_LINES, obbMesh.getVertexCount(), GL_UNSIGNED_INT, 0);

                //restore state
                glBindVertexArray(0);
                glBindTexture(GL_TEXTURE_2D, 0);
                ((Collider) gameItem).setShowBB(null);
            }
        }

        for (AbstractGameItem terrainBlock : scene.getTerrain().getGameItems()) {
            terrainBlock.render(shaderProgram);
        }

        shaderProgram.unbind();
    }

    private void renderLights(Matrix4f viewMatrix, Vector3f ambientLight, DirectionalLight directionalLight) {

        //update light uniforms
        shaderProgram.setUniform("ambientLight", ambientLight);
        shaderProgram.setUniform("specularPower", specularPower);


        //directional light and transform to view coords
        DirectionalLight currDirLight = new DirectionalLight(directionalLight);
        Vector4f dir = new Vector4f(currDirLight.getDirection(), 0);
        dir.mul(viewMatrix);
        currDirLight.setDirection(new Vector3f(dir.x, dir.y, dir.z));
        shaderProgram.setUniform("directionalLight", currDirLight);
    }

    private void renderHud(Matrix4f orthoProjMatrix, IHud hud) {
        hudShaderProgram.bind();

        for (HudElement hudElement : hud.getHudElements()) {

            // Set ortohtaphic and model matrix for this HUD item
            Matrix4f projModelMatrix = Transformation.getOrtoProjModelMatrix(hudElement, orthoProjMatrix);
            Mesh hudElementMesh = hudElement.getMesh();
            hudShaderProgram.setUniform("projModelMatrix", projModelMatrix);
            hudShaderProgram.setUniform("colour", hudElementMesh.getMaterial().getAmbientColor());
            hudShaderProgram.setUniform("hasTexture", hudElementMesh.getMaterial().isTextured() ? 1 : 0);

            hudElement.render(hudShaderProgram);
        }

        hudShaderProgram.unbind();
    }

    private void renderSkyBox(Matrix4f projectionMatrix, Matrix4f viewMatrix, Scene scene) {
        SkyBox skyBox = scene.getSkyBox();
        skyBoxShaderProgram.bind();

        // todo: [spaghet] SkyBox ShaderProgram needs ambient light and projection matrix,
        //  these setters are used to provide them without having to change the render method signature.
        //  Bad because calling render without using these setters = fail engine.
        skyBoxShaderProgram.setUniform("texture_sampler", 0);
        skyBoxShaderProgram.setUniform("projectionMatrix", projectionMatrix);

        Matrix4f vMatrix = new Matrix4f(viewMatrix);
        vMatrix.m30(0);
        vMatrix.m31(0);
        vMatrix.m32(0);
        Matrix4f modelViewMatrix = Transformation.getModelViewMatrix(skyBox, vMatrix);
        skyBoxShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
        skyBoxShaderProgram.setUniform("ambientLight", scene.getSceneLight().getAmbientLight());

        skyBox.render(skyBoxShaderProgram);

        skyBoxShaderProgram.unbind();
    }

    public void cleanup() {
        if (shaderProgram != null) {
            shaderProgram.cleanup();
        }
    }
}
