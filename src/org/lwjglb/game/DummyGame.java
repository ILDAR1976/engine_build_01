package org.lwjglb.game;

import java.io.File;

import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.assimp.AIQuaternion;

import static org.lwjgl.glfw.GLFW.*;
import org.lwjglb.engine.IGameLogic;
import org.lwjglb.engine.MouseInput;
import org.lwjglb.engine.Scene;
import org.lwjglb.engine.SceneLight;
import org.lwjglb.engine.Utils;
import org.lwjglb.engine.EWindow;
import org.lwjglb.engine.graph.Camera;
import org.lwjglb.engine.graph.Mesh;
import org.lwjglb.engine.graph.Renderer;
import org.lwjglb.engine.graph.anim.AnimGameItem;
import org.lwjglb.engine.graph.anim.Animation;
import org.lwjglb.engine.graph.lights.DirectionalLight;
import org.lwjglb.engine.graph.weather.Fog;
import org.lwjglb.engine.items.GameItem;
import org.lwjglb.engine.items.SkyBox;
import org.lwjglb.engine.loaders.assimp.AnimMeshesLoader;
import org.lwjglb.engine.loaders.assimp.StaticMeshesLoader;

public class DummyGame implements IGameLogic {

    private static final float MOUSE_SENSITIVITY = 0.2f;

    private final Vector3f cameraInc;

    private final Renderer renderer;

    private final Camera camera;

    private Scene scene;

    private static final float CAMERA_POS_STEP = 0.40f;

    private float angleInc;

    private float lightAngle;

    private boolean firstTime;

    private boolean sceneChanged;

    private Animation animation;

    private AnimGameItem animItem;

    private GameItem[] gameItems;

    public DummyGame() {
        renderer = new Renderer();
        camera = new Camera();
        cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
        angleInc = 0;
        lightAngle = 90;
        firstTime = true;
    }

    @Override
    public void init(EWindow EWindow) throws Exception {
        renderer.init(EWindow);

        scene = new Scene();
        File file = new File("./source/models/terrain/terrain.obj");
        Mesh[] terrainMesh = StaticMeshesLoader.load(file.getAbsolutePath(), "./source/models/terrain");
        GameItem terrain = new GameItem(terrainMesh);
        terrain.setScale(100.0f);
        
        file = new File("./source/models/house/house.obj");
        Mesh[] mesh = StaticMeshesLoader.load(file.getAbsolutePath(), "./source/models/house");
        GameItem house = new GameItem(mesh);
        house.setScale(0.2f);
        house.setPosition(-4f, 0f, 0f);
        
        file = new File("./source/models/bob/boblamp.md5mesh");
        
        animItem = AnimMeshesLoader.loadAnimGameItem(file.getAbsolutePath(), "./source/");
        animItem.setScale(0.05f);
        animItem.setPosition(0f, 0f, 0f);
        animation = animItem.getCurrentAnimation();
        
         Quaternionf qt = new Quaternionf();
        
         Vector3f rv = new Vector3f(0f,1f,0f);
        
        rv.normalize();
        
        float rag = 90f;
        
        float ra = (rag * 3.1415926f) / 180; 
        
        qt.set( rv.x * (float) Math.sin(ra / 2), 
        		rv.y * (float) Math.sin(ra / 2),
        		rv.z * (float) Math.sin(ra / 2), 
        		(float) Math.cos(ra / 2));
        
        animItem.setRotation(qt);
         
        
        
        scene.setGameItems(new GameItem[]{animItem, house,  terrain});
        
 
        // Shadows
        scene.setRenderShadows(true);

        // Fog
        Vector3f fogColour = new Vector3f(0.5f, 0.5f, 0.5f);
        scene.setFog(new Fog(true, fogColour, 0.02f));

        // Setup  SkyBox
        float skyBoxScale = 100.0f;
        file = new File("./source/models/skybox.obj");
        SkyBox skyBox = new SkyBox(file.getAbsolutePath(), new Vector4f(0.65f, 0.65f, 0.65f, 1.0f));
        skyBox.setScale(skyBoxScale);
        scene.setSkyBox(skyBox);

        // Setup Lights
        setupLights();

        camera.getPosition().x = -1.5f;
        camera.getPosition().y = 3.0f;
        camera.getPosition().z = 4.5f;
        camera.getRotation().x = 15.0f;
        camera.getRotation().y = 390.0f;
    }

    private void setupLights() {
        SceneLight sceneLight = new SceneLight();
        scene.setSceneLight(sceneLight);

        // Ambient Light
        sceneLight.setAmbientLight(new Vector3f(0.3f, 0.3f, 0.3f));
        sceneLight.setSkyBoxLight(new Vector3f(1.0f, 1.0f, 1.0f));

        // Directional Light
        float lightIntensity = 1.0f;
        Vector3f lightDirection = new Vector3f(0, 1, 1);
        DirectionalLight directionalLight = new DirectionalLight(new Vector3f(1, 1, 1), lightDirection, lightIntensity);
        sceneLight.setDirectionalLight(directionalLight);
    }

    @Override
    public void input(EWindow EWindow, MouseInput mouseInput) {
        sceneChanged = false;
        cameraInc.set(0, 0, 0);
        if (EWindow.isKeyPressed(GLFW_KEY_W)) {
            sceneChanged = true;
            cameraInc.z = -1;
        } else if (EWindow.isKeyPressed(GLFW_KEY_S)) {
            sceneChanged = true;
            cameraInc.z = 1;
        }
        if (EWindow.isKeyPressed(GLFW_KEY_A)) {
            sceneChanged = true;
            cameraInc.x = -1;
        } else if (EWindow.isKeyPressed(GLFW_KEY_D)) {
            sceneChanged = true;
            cameraInc.x = 1;
        }
        if (EWindow.isKeyPressed(GLFW_KEY_Z)) {
            sceneChanged = true;
            cameraInc.y = -1;
        } else if (EWindow.isKeyPressed(GLFW_KEY_X)) {
            sceneChanged = true;
            cameraInc.y = 1;
        }
        if (EWindow.isKeyPressed(GLFW_KEY_UP)) {
            sceneChanged = true;
 
            cameraInc.x = 1;
            cameraInc.y = -1;
            cameraInc.z = 1;

        } else if (EWindow.isKeyPressed(GLFW_KEY_DOWN)) {
            sceneChanged = true;
            cameraInc.x = -1;
            cameraInc.y = 1;
            cameraInc.z = -1;
        }
        if (EWindow.isKeyPressed(GLFW_KEY_LEFT)) {
            sceneChanged = true;
            angleInc -= 0.05f;
        } else if (EWindow.isKeyPressed(GLFW_KEY_RIGHT)) {
            sceneChanged = true;
            angleInc += 0.05f;
        } else {
            angleInc = 0;            
        }
        if (EWindow.isKeyPressed(GLFW_KEY_SPACE)) {
            sceneChanged = true;
            animation.nextFrame();
        }
        
        animation.nextFrame();
    }

    @Override
    public void update(float interval, MouseInput mouseInput, EWindow EWindow) {
        if (mouseInput.isRightButtonPressed()) {
            // Update camera based on mouse            
            Vector2f rotVec = mouseInput.getDisplVec();
            camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
            sceneChanged = true;
        }

        // Update camera position
        camera.movePosition(cameraInc.x * CAMERA_POS_STEP, cameraInc.y * CAMERA_POS_STEP, cameraInc.z * CAMERA_POS_STEP);

        lightAngle += angleInc;
        if (lightAngle < 0) {
            lightAngle = 0;
        } else if (lightAngle > 180) {
            lightAngle = 180;
        }
        float zValue = (float) Math.cos(Math.toRadians(lightAngle));
        float yValue = (float) Math.sin(Math.toRadians(lightAngle));
        Vector3f lightDirection = this.scene.getSceneLight().getDirectionalLight().getDirection();
        lightDirection.x = 0;
        lightDirection.y = yValue;
        lightDirection.z = zValue;
        lightDirection.normalize();

        // Update view matrix
        camera.updateViewMatrix();
    }

    @Override
    public void render(EWindow EWindow) {
        if (firstTime) {
            sceneChanged = true;
            firstTime = false;
        }
        renderer.render(EWindow, camera, scene, sceneChanged);
    }

    @Override
    public void cleanup() {
        renderer.cleanup();

        scene.cleanup();
    }
}
