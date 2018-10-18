package com.pku.xuzhimin.vrplayer;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TEXTURE_2D;

/**
 * Created by zhimin xu on 2018/9/20.
 */
public class Sphere {
    private static final int sPositionDataSize = 3;
    private static final int sTextureCoordinateDataSize = 2;

    private FloatBuffer mVerticesBuffer;
    private FloatBuffer mTexCoordinateBuffer;
    private int uTextureSamplerHandle;
    private ShortBuffer indexBuffer;
    private int mNumIndices;

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0.3371875f, 0.76953125f, 0.4444444f, 1.0f };

    protected static final String VERTEX_SHADER =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec2 aTextureCoord;" +
                    "void main() {" +
                    // the matrix must be included as a modifier of gl_Position
                    // Note that the uMVPMatrix factor *must be first* in order
                    // for the matrix multiplication product to be correct.
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    protected static final String FRAGMENT_SHADER =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private final String vertexShaderCode_pic =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec2 aTextureCoord;" +
                    "varying vec2 vTexCoord;" +
                    "void main() {" +
                    // the matrix must be included as a modifier of gl_Position
                    // Note that the uMVPMatrix factor *must be first* in order
                    // for the matrix multiplication product to be correct.
                    "  vTexCoord = aTextureCoord;" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    private final String fragmentShaderCode_pic =
            "precision mediump float;" +
                    "varying vec2 vTexCoord;" +
                    "uniform sampler2D sTexture;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "  gl_FragColor = texture2D(sTexture,vTexCoord);" +
                    "}";

    private final int mProgram;

    public Sphere(float radius, int rings, int sectors) {
        final float PI = (float) Math.PI;
        final float PI_2 = (float) (Math.PI / 2);

        float R = 1f / (float) rings;
        float S = 1f / (float) sectors;
        short r, s;
        float x, y, z;

        int numPoint = (rings + 1) * (sectors + 1);
        float[] vertexs = new float[numPoint * 3];
        float[] texcoords = new float[numPoint * 2];
        short[] indices = new short[numPoint * 6];

        int t = 0, v = 0;
        for (r = 0; r < rings + 1; r++) {
            for (s = 0; s < sectors + 1; s++) {
                x = (float) (Math.cos(2 * PI * s * S) * Math.sin(PI * r * R));
                y = (float) Math.sin(-PI_2 + PI * r * R);
                z = (float) (Math.sin(2 * PI * s * S) * Math.sin(PI * r * R));

                texcoords[t++] = s * S;
                texcoords[t++] = r * R;

                vertexs[v++] = x * radius;
                vertexs[v++] = y * radius;
                vertexs[v++] = z * radius;
            }
        }

        int counter = 0;
        int sectorsPlusOne = sectors + 1;
        for (r = 0; r < rings; r++) {
            for (s = 0; s < sectors; s++) {
                indices[counter++] = (short) (r * sectorsPlusOne + s);
                indices[counter++] = (short) ((r + 1) * sectorsPlusOne + (s));
                indices[counter++] = (short) ((r) * sectorsPlusOne + (s + 1));
                indices[counter++] = (short) ((r) * sectorsPlusOne + (s + 1));
                indices[counter++] = (short) ((r + 1) * sectorsPlusOne + (s));
                indices[counter++] = (short) ((r + 1) * sectorsPlusOne + (s + 1));
            }
        }

        ByteBuffer bb = ByteBuffer.allocateDirect(vertexs.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertexs);
        vertexBuffer.position(0);

        ByteBuffer cc = ByteBuffer.allocateDirect(texcoords.length * 4);
        cc.order(ByteOrder.nativeOrder());
        FloatBuffer texBuffer = cc.asFloatBuffer();
        texBuffer.put(texcoords);
        texBuffer.position(0);

        ByteBuffer dlb = ByteBuffer.allocateDirect(indices.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        indexBuffer = dlb.asShortBuffer();
        indexBuffer.put(indices);
        indexBuffer.position(0);

        mTexCoordinateBuffer = texBuffer;
        mVerticesBuffer = vertexBuffer;
        mNumIndices = indices.length;


        //int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode_pic);
        //int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode_pic);

        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram();

        // add the vertex shader to program
        GLES20.glAttachShader(mProgram, vertexShader);

        // add the fragment shader to program
        GLES20.glAttachShader(mProgram, fragmentShader);

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(mProgram);
    }


    public void uploadVerticesBuffer(int positionHandle) {
        FloatBuffer vertexBuffer = getVerticesBuffer();
        if (vertexBuffer == null) return;
        vertexBuffer.position(0);

        GLES20.glVertexAttribPointer(positionHandle, sPositionDataSize, GLES20.GL_FLOAT, false, 3*4, vertexBuffer);
        GLES20.glEnableVertexAttribArray(positionHandle);
    }

    public void uploadTexCoordinateBuffer(int textureCoordinateHandle) {
        FloatBuffer textureBuffer = getTexCoordinateBuffer();
        if (textureBuffer == null) return;
        textureBuffer.position(0);

        GLES20.glVertexAttribPointer(textureCoordinateHandle, sTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 2*4, textureBuffer);
        GLES20.glEnableVertexAttribArray(textureCoordinateHandle);
    }


    public FloatBuffer getVerticesBuffer() {
        return mVerticesBuffer;
    }

    public FloatBuffer getTexCoordinateBuffer() {
        return mTexCoordinateBuffer;
    }

    public void draw() {
        if (indexBuffer != null) {
            indexBuffer.position(0);
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, mNumIndices, GLES20.GL_UNSIGNED_SHORT, indexBuffer);
        } else {
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mNumIndices);
        }
    }

    private int mPositionHandle;
    private int mTextureCoordinateHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    public void draw2(float[] mvpMatrix) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        uploadVerticesBuffer(mPositionHandle);

        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        uploadTexCoordinateBuffer(mTextureCoordinateHandle);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        //GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mNumIndices, GLES20.GL_UNSIGNED_SHORT, indexBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordinateHandle);
    }

    static final int COORDS_PER_VERTEX = 3;
    static final int COORDS_PER_TEXTURE = 2;

    static float [] triangleCoords = new float[(100 + 1) * (200 + 1)*3];
    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    private final int textureStride = COORDS_PER_TEXTURE * 4; // 4 bytes per vertex

    public void draw3(float[] mvpMatrix) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, mVerticesBuffer);

        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, COORDS_PER_TEXTURE, GLES20.GL_FLOAT, false, textureStride, mTexCoordinateBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
    public void draw4(float[] mvpMatrix, int textureid) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, mVerticesBuffer);

        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, COORDS_PER_TEXTURE, GLES20.GL_FLOAT, false, textureStride, mTexCoordinateBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        uTextureSamplerHandle=GLES20.glGetUniformLocation(mProgram,"sTexture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureid);
        GLES20.glUniform1i(uTextureSamplerHandle,0);

        //GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
        // Draw the triangle
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mNumIndices, GLES20.GL_UNSIGNED_SHORT, indexBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glBindBuffer(GL_ARRAY_BUFFER, 0);
        GLES20.glBindTexture(GL_TEXTURE_2D, 0);
    }
}
