package net.kogics.kojo.examplej;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class CustomShaderNDCVisualizerExample extends ApplicationAdapter {
    private static class CustomShapeRenderer extends ShapeRenderer {
        private final ShaderProgram shader;

        public CustomShapeRenderer() {
            super();

            // Create and compile shader
            String vertexShader =
                    "attribute vec4 a_position;\n" +
                            "attribute vec4 a_color;\n" +          // Added color attribute
                            "uniform mat4 u_projModelView;\n" +    // Changed from u_projTrans
                            "varying vec4 v_color;\n" +
                            "\n" +
                            "void main() {\n" +
                            "    vec4 ndc = u_projModelView * a_position;\n" +  // Changed from u_projTrans
                            "    v_color = vec4(\n" +
                            "        (ndc.x + 1.0) * 0.5,\n" +
                            "        (ndc.y + 1.0) * 0.5,\n" +
                            "        0.0,\n" +
                            "        1.0\n" +
                            "    );\n" +
                            "    gl_Position = ndc;\n" +
                            "}";

            String fragmentShader =
                    "#ifdef GL_ES\n" +
                            "precision mediump float;\n" +
                            "#endif\n" +
                            "\n" +
                            "varying vec4 v_color;\n" +
                            "\n" +
                            "void main() {\n" +
                            "    gl_FragColor = v_color;\n" +
                            "}";

            shader = new ShaderProgram(vertexShader, fragmentShader);
            if (!shader.isCompiled()) {
                Gdx.app.error("Shader", "Compilation failed:\n" + shader.getLog());
            }

            // Get the internal ImmediateModeRenderer20
            ImmediateModeRenderer20 renderer = (ImmediateModeRenderer20) this.getRenderer();

            // Set our custom shader on the renderer
            renderer.setShader(shader);
        }

        @Override
        public void dispose() {
            super.dispose();
            shader.dispose();
        }
    }

    private CustomShapeRenderer shapeRenderer;
    private OrthographicCamera camera;


    @Override
    public void create() {
        // Create shape renderer and camera
        shapeRenderer = new CustomShapeRenderer();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void render() {
        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update camera
        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);

        // Draw shapes
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Draw some rectangles
        shapeRenderer.rect(100, 100, 200, 150);  // Left bottom rectangle
        shapeRenderer.rect(400, 300, 200, 150);  // Right top rectangle

        // Draw a triangle
        shapeRenderer.triangle(
                300, 200,  // point 1
                500, 200,  // point 2
                400, 400   // point 3
        );

        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}