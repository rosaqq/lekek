package net.sknv.game;

import net.sknv.engine.IHud;
import net.sknv.engine.Scene;
import net.sknv.engine.SkyBox;
import net.sknv.engine.Utils;
import net.sknv.engine.entities.AbstractGameItem;
import net.sknv.engine.entities.HudElement;
import net.sknv.engine.graph.DirectionalLight;
import net.sknv.engine.graph.ShaderProgram;
import net.sknv.engine.graph.Transformation;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.opengl.GL11.*;

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
            gameItem.render(shaderProgram, viewMatrix);
        }

        for (AbstractGameItem terrainBlock : scene.getTerrain().getGameItems()) {
            terrainBlock.render(shaderProgram, viewMatrix);
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

    private void renderHud(Matrix4f ortho, IHud hud) {
        hudShaderProgram.bind();

        for (HudElement elem : hud.getHudElements()) {
            elem.render(hudShaderProgram, ortho);
        }

        hudShaderProgram.unbind();
    }

    private void renderSkyBox(Matrix4f projectionMatrix, Matrix4f viewMatrix, Scene scene) {
        SkyBox skyBox = scene.getSkyBox();
        skyBoxShaderProgram.bind();

        // todo: [spaghet] SkyBox ShaderProgram needs ambient light and projection matrix,
        //  these setters are used to provide them without having to change the render method signature.
        //  Bad because calling render without using these setters = fail engine.
        shaderProgram.setUniform("texture_sampler", 0);
        shaderProgram.setUniform("projectionMatrix", projectionMatrix);

        Matrix4f vMatrix = new Matrix4f(viewMatrix);
        vMatrix.m30(0);
        vMatrix.m31(0);
        vMatrix.m32(0);
        Matrix4f modelViewMatrix = Transformation.getModelViewMatrix(skyBox, vMatrix);
        shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
        shaderProgram.setUniform("ambientLight", scene.getSceneLight().getAmbientLight());

        skyBox.render(skyBoxShaderProgram, viewMatrix);

        skyBoxShaderProgram.unbind();
    }

    public void cleanup() {
        if (shaderProgram != null) {
            shaderProgram.cleanup();
        }
    }
}
