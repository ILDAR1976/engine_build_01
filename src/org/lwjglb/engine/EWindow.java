package org.lwjglb.engine;

import org.joml.Matrix4f;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class EWindow {

    /**
     * Field of View in Radians
     */
    public static final float FOV = (float) Math.toRadians(60.0f);

    /**
     * Distance to the near plane
     */
    public static final float Z_NEAR = 0.01f;

    /**
     * Distance to the far plane
     */
    public static final float Z_FAR = 1000.f;

    private final String title;

    private int width;

    private int height;

    private long EWindowHandle;

    private boolean resized;

    private boolean vSync;

    private final EWindowOptions opts;

    private final Matrix4f projectionMatrix;

    public EWindow(String title, int width, int height, boolean vSync, EWindowOptions opts) {
        this.title = title;
        this.width = width;
        this.height = height;
        this.vSync = vSync;
        this.resized = false;
        this.opts = opts;
        projectionMatrix = new Matrix4f();
    }

    public void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints(); // optional, the current EWindow hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the EWindow will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE); // the EWindow will be resizable
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        if (opts.compatibleProfile) {
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_COMPAT_PROFILE);
        } else {
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        }

        boolean maximized = false;
        // If no size has been specified set it to maximized state
        if (width == 0 || height == 0) {
            // Set up a fixed width and height so EWindow initialization does not fail
            width = 100;
            height = 100;
            glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
            maximized = true;
        }

        // Create the EWindow
        EWindowHandle = glfwCreateWindow(width, height, title, NULL, NULL);
        if (EWindowHandle == NULL) {
            throw new RuntimeException("Failed to create the GLFW EWindow");
        }

        glfwSetFramebufferSizeCallback(EWindowHandle, (EWindow, width, height) -> {
            this.width = width;
            this.height = height;
            this.setResized(true);
        });

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(EWindowHandle, (EWindow, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(EWindow, true); // We will detect this in the rendering loop
            }
        });

        if (!maximized) {
            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            // Center our EWindow
            glfwSetWindowPos(
                    EWindowHandle,
                    (vidmode.width() - width) / 2,
                    (vidmode.height() - height) / 2
            );
        }

        // Make the OpenGL context current
        glfwMakeContextCurrent(EWindowHandle);

        if (isvSync()) {
            // Enable v-sync
            glfwSwapInterval(1);
        }

        // Make the EWindow visible
        glfwShowWindow(EWindowHandle);

        GL.createCapabilities();

        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_STENCIL_TEST);
        if (opts.showTriangles) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        }

        // Support for transparencies
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        if (opts.cullFace) {
            glEnable(GL_CULL_FACE);
            glCullFace(GL_BACK);
        }

        // Antialiasing
        if (opts.antialiasing) {
            glfwWindowHint(GLFW_SAMPLES, 4);
        }
    }
    
    public void restoreState() {
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_STENCIL_TEST);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        if (opts.cullFace) {
            glEnable(GL_CULL_FACE);
            glCullFace(GL_BACK);
        }
    }

    public long getEWindowHandle() {
        return EWindowHandle;
    }

    public String getEWindowTitle() {
        return title;
    }

    public void setEWindowTitle(String title) {
        glfwSetWindowTitle(EWindowHandle, title);
    }

    public EWindowOptions getEWindowOptions() {
        return opts;
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public Matrix4f updateProjectionMatrix() {
        float aspectRatio = (float) width / (float) height;
        return projectionMatrix.setPerspective(FOV, aspectRatio, Z_NEAR, Z_FAR);
    }

    public static Matrix4f updateProjectionMatrix(Matrix4f matrix, int width, int height) {
        float aspectRatio = (float) width / (float) height;
        return matrix.setPerspective(FOV, aspectRatio, Z_NEAR, Z_FAR);
    }

    public void setClearColor(float r, float g, float b, float alpha) {
        glClearColor(r, g, b, alpha);
    }

    public boolean isKeyPressed(int keyCode) {
        return glfwGetKey(EWindowHandle, keyCode) == GLFW_PRESS;
    }

    public boolean WindowShouldClose() {
        return glfwWindowShouldClose(EWindowHandle);
    }

    public String getTitle() {
        return title;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isResized() {
        return resized;
    }

    public void setResized(boolean resized) {
        this.resized = resized;
    }

    public boolean isvSync() {
        return vSync;
    }

    public void setvSync(boolean vSync) {
        this.vSync = vSync;
    }

    public void update() {
        glfwSwapBuffers(EWindowHandle);
        glfwPollEvents();
    }

    public EWindowOptions getOptions() {
        return opts;
    }
    
    public static class EWindowOptions {

        public boolean cullFace;

        public boolean showTriangles;

        public boolean showFps;

        public boolean compatibleProfile;

        public boolean antialiasing;

        public boolean frustumCulling;        
    }
}
