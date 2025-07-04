/* San Angeles Observation OpenGL ES version example
 * Copyright 2004-2005 Jetro Lauha
 * All rights reserved.
 * Web: http://iki.fi/jetro/
 *
 * This source is free software; you can redistribute it and/or
 * modify it under the terms of EITHER:
 *   (1) The GNU Lesser General Public License as published by the Free
 *       Software Foundation; either version 2.1 of the License, or (at
 *       your option) any later version. The text of the GNU Lesser
 *       General Public License is included with this source in the
 *       file LICENSE-LGPL.txt.
 *   (2) The BSD-style license that is included with this source in
 *       the file LICENSE-BSD.txt.
 *
 * This source is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the files
 * LICENSE-LGPL.txt and LICENSE-BSD.txt for more details.
 *
 * $Id$
 * $Revision$
 */

package demos.es1.angeles;

import com.jogamp.common.nio.Buffers;
import com.jogamp.math.FixedPoint;
import com.jogamp.opengl.*;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.fixedfunc.GLPointerFunc;
import com.jogamp.opengl.glu.*;

import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.glsl.fixedfunc.*;

import java.nio.*;

public class AngelesGL implements GLEventListener {

    public AngelesGL() {
        this(true);
    }

    public AngelesGL(final boolean enableBlending) {
        blendingEnabled = enableBlending;
        quadVertices = Buffers.newDirectFloatBuffer(12);
        quadVertices.put(new float[]{
            -1.0f, -1.0f,
             1.0f, -1.0f,
            -1.0f,  1.0f,
             1.0f, -1.0f,
             1.0f,  1.0f,
            -1.0f,  1.0f
        });
        quadVertices.flip();

        light0Position=Buffers.newDirectFloatBuffer(4);
        light0Diffuse=Buffers.newDirectFloatBuffer(4);
        light1Position=Buffers.newDirectFloatBuffer(4);
        light1Diffuse=Buffers.newDirectFloatBuffer(4);
        light2Position=Buffers.newDirectFloatBuffer(4);
        light2Diffuse=Buffers.newDirectFloatBuffer(4);
        materialSpecular=Buffers.newDirectFloatBuffer(4);

        light0Position.put(new float[] { FixedPoint.toFloat(-0x40000), 1.0f, 1.0f, 0.0f });
        light0Diffuse.put(new float[] { 1.0f, FixedPoint.toFloat(0x6666), 0.0f, 1.0f });
        light1Position.put(new float[] { 1.0f, FixedPoint.toFloat(-0x20000), -1.0f, 0.0f });
        light1Diffuse.put(new float[] { FixedPoint.toFloat(0x11eb), FixedPoint.toFloat(0x23d7), FixedPoint.toFloat(0x5999), 1.0f });
        light2Position.put(new float[] { -1.0f, 0.0f, FixedPoint.toFloat(-0x40000), 0.0f });
        light2Diffuse.put(new float[] { FixedPoint.toFloat(0x11eb), FixedPoint.toFloat(0x2b85), FixedPoint.toFloat(0x23d7), 1.0f });
        materialSpecular.put(new float[] { 1.0f, 1.0f, 1.0f, 1.0f });

        light0Position.flip();
        light0Diffuse.flip();
        light1Position.flip();
        light1Diffuse.flip();
        light2Position.flip();
        light2Diffuse.flip();
        materialSpecular.flip();

        seedRandom(15);

        width=0;
        height=0;
        x=0;
        y=0;
    }

    @Override
    public void init(final GLAutoDrawable drawable) {
        // FIXME: gl.setSwapInterval(1);

        cComps = drawable.getGL().isGLES1() ? 4: 3;

        this.gl = FixedFuncUtil.wrapFixedFuncEmul(drawable.getGL(), ShaderSelectionMode.AUTO, null);
        System.err.println("AngelesGL: "+this.gl);

        this.glu = GLU.createGLU();

        gl.glEnable(GLLightingFunc.GL_NORMALIZE);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDisable(GL.GL_CULL_FACE);
        gl.glCullFace(GL.GL_BACK);
        gl.glShadeModel(GLLightingFunc.GL_FLAT);

        gl.glEnable(GLLightingFunc.GL_LIGHTING);
        gl.glEnable(GLLightingFunc.GL_LIGHT0);
        gl.glEnable(GLLightingFunc.GL_LIGHT1);
        gl.glEnable(GLLightingFunc.GL_LIGHT2);

        gl.glEnableClientState(GLPointerFunc.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GLPointerFunc.GL_COLOR_ARRAY);

        for (int a = 0; a < SuperShape.COUNT; ++a)
        {
            sSuperShapeObjects[a] = createSuperShape(SuperShape.sParams[a]);
        }
        sGroundPlane = createGroundPlane();

        gAppAlive = 1;

        sStartTick = System.currentTimeMillis();
        frames=0;

        /*
        gl.glGetError(); // flush error ..
        if(gl.isGLES2()) {
            GLES2 gles2 = gl.getGLES2();

            // Debug ..
            //DebugGLES2 gldbg = new DebugGLES2(gles2);
            //gles2.getContext().setGL(gldbg);
            //gles2 = gldbg;

            // Trace ..
            TraceGLES2 gltrace = new TraceGLES2(gles2, System.err);
            gles2.getContext().setGL(gltrace);
            gles2 = gltrace;
        } */
    }

    @Override
    public void reshape(final GLAutoDrawable drawable, final int x, final int y, final int width, final int height) {
        this.width = width;
        this.height=height;
        this.x = x;
        this.y = y;

        this.gl = drawable.getGL().getGL2ES1();

        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
        gl.glLoadIdentity();

        gl.glClearColor(0.1f, 0.2f, 0.3f, 1.0f);

        // JAU gl.glHint(GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_FASTEST);

        //gl.glShadeModel(gl.GL_SMOOTH);
        gl.glShadeModel(GLLightingFunc.GL_FLAT);
        gl.glDisable(GL.GL_DITHER);

        //gl.glMatrixMode(gl.GL_PROJECTION);
        //gl.glLoadIdentity();
        //glu.gluPerspective(45.0f, (float)width / (float)height, 0.5f, 150.0f);

        //System.out.println("reshape ..");
    }

    @Override
    public void dispose(final GLAutoDrawable drawable) {
    }

    @Override
    public void display(final GLAutoDrawable drawable) {
        long tick = System.currentTimeMillis();

        if (gAppAlive==0)
            return;

        this.gl = drawable.getGL().getGL2ES1();

        // Actual tick value is "blurred" a little bit.
        sTick = (sTick + tick - sStartTick) >> 1;

        // Terminate application after running through the demonstration once.
        if (sTick >= RUN_LENGTH)
        {
            gAppAlive = 0;
            return;
        }

        gl.glClear(GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT);

        gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(45.0f, (float)width / (float)height, 0.5f, 150.0f);

        // Update the camera position and set the lookat.
        camTrack();

        // Configure environment.
        configureLightAndMaterial();

        if(blendingEnabled) {
            gl.glEnable(GL.GL_CULL_FACE);
            // Draw the reflection by drawing models with negated Z-axis.
            gl.glPushMatrix();
            drawModels(-1);
            gl.glPopMatrix();
        }

        // Draw the ground plane to the window. (opt. blending)
        drawGroundPlane();

        if(blendingEnabled) {
            gl.glDisable(GL.GL_CULL_FACE);
        }

        // Draw all the models normally.
        drawModels(1);

        if(blendingEnabled) {
            // Draw fade quad over whole window (when changing cameras).
            drawFadeQuad();
        }

        frames++;
        tick = System.currentTimeMillis();
    }

    public void displayChanged(final GLAutoDrawable drawable, final boolean modeChanged, final boolean deviceChanged) {
    }

 private boolean blendingEnabled = true;
 private GL2ES1 gl; // temp cache
 private GLU glu;

 // Total run length is 20 * camera track base unit length (see cams.h).
 private final int RUN_LENGTH  = (20 * CamTrack.CAMTRACK_LEN) ;
 private final int RANDOM_UINT_MAX = 65535 ;

 private long sRandomSeed = 0;

void seedRandom(final long seed)
{
    sRandomSeed = seed;
}

int randomUInt()
{
    sRandomSeed = sRandomSeed * 0x343fd + 0x269ec3;
    return Math.abs((int) (sRandomSeed >> 16));
}

private int cComps;

// Definition of one GL object in this demo.
public class GLSpatial {
    /* Vertex array and color array are enabled for all objects, so their
     * pointers must always be valid and non-null. Normal array is not
     * used by the ground plane, so when its pointer is null then normal
     * array usage is disabled.
     *
     * Vertex array is supposed to use GL.GL_FLOAT datatype and stride 0
     * (i.e. tightly packed array). Color array is supposed to have 4
     * components per color with GL.GL_UNSIGNED_BYTE datatype and stride 0.
     * Normal array is supposed to use GL.GL_FLOAT datatype and stride 0.
     */
    private final int vboName;
    private int count;
    private final int vComps, nComps;
    private ByteBuffer pBuffer=null;
    private FloatBuffer vertexArray=null;
    private FloatBuffer colorArray=null;
    private FloatBuffer normalArray=null;
    protected GLArrayDataWrapper vArrayData, cArrayData, nArrayData=null;

    public GLSpatial(final int vertices, final int vertexComponents,
                    final boolean useNormalArray) {
        count = vertices;
        vComps= vertexComponents;
        nComps = useNormalArray ? 3 : 0;

        final int bSize = GLBuffers.sizeOfGLType(GL.GL_FLOAT) * count * ( vComps + cComps + nComps) ;
        pBuffer = Buffers.newDirectByteBuffer(bSize);

        int pos = 0;
        int size= GLBuffers.sizeOfGLType(GL.GL_FLOAT) * count * vComps ;
        vertexArray = (FloatBuffer) GLBuffers.sliceGLBuffer(pBuffer, pos, size, GL.GL_FLOAT);
        final int vOffset = 0;
        pos+=size;

        size=GLBuffers.sizeOfGLType(GL.GL_FLOAT) * count * cComps ;
        colorArray = (FloatBuffer) GLBuffers.sliceGLBuffer(pBuffer, pos, size, GL.GL_FLOAT);
        final int cOffset=pos;
        pos+=size;

        int nOffset=0;
        if(useNormalArray) {
            size=GLBuffers.sizeOfGLType(GL.GL_FLOAT) * count * nComps ;
            normalArray = (FloatBuffer) GLBuffers.sliceGLBuffer(pBuffer, pos, size, GL.GL_FLOAT);
            nOffset=pos;
            pos+=size;
        }
        pBuffer.position(pos);
        pBuffer.flip();

        final int[] tmp = new int[1];
        gl.glGenBuffers(1, tmp, 0);
        vboName = tmp[0];

        vArrayData = GLArrayDataWrapper.createFixed(GLPointerFunc.GL_VERTEX_ARRAY, vComps, GL.GL_FLOAT, false,
                                                    0, pBuffer, vboName, vOffset, GL.GL_STATIC_DRAW, GL.GL_ARRAY_BUFFER);
        cArrayData = GLArrayDataWrapper.createFixed(GLPointerFunc.GL_COLOR_ARRAY, cComps, GL.GL_FLOAT, false,
                                                    0, pBuffer, vboName, cOffset, GL.GL_STATIC_DRAW, GL.GL_ARRAY_BUFFER);
        if(useNormalArray) {
            nArrayData = GLArrayDataWrapper.createFixed(GLPointerFunc.GL_NORMAL_ARRAY, nComps, GL.GL_FLOAT, false,
                                                        0, pBuffer, vboName, nOffset, GL.GL_STATIC_DRAW, GL.GL_ARRAY_BUFFER);
        }
    }

    void setCount(final int c) {
        if(count != c) {
            throw new RuntimeException("diff count: "+count+" -> "+c);
        }
        count = c;
    }

    private boolean sealed = false;

    void seal()
    {
        if(sealed) return;
        sealed = true;

        vertexArray.position(count);
        vertexArray.flip();
        colorArray.position(count);
        colorArray.flip();
        if(nComps>0) {
            normalArray.position(count);
            normalArray.flip();
        }

        if(nComps>0) {
            gl.glEnableClientState(GLPointerFunc.GL_NORMAL_ARRAY);
        }

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboName);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, pBuffer.limit(), pBuffer, GL.GL_STATIC_DRAW);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);

        if(nComps>0) {
            gl.glDisableClientState(GLPointerFunc.GL_NORMAL_ARRAY);
        }
    }

    void draw()
    {
        seal();
        if(nComps>0) {
           gl.glEnableClientState(GLPointerFunc.GL_NORMAL_ARRAY);
        }

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboName);

        gl.glVertexPointer(vArrayData);
        gl.glColorPointer(cArrayData);
        if(nComps>0) {
            gl.glNormalPointer(nArrayData);
        }


        gl.glDrawArrays(GL.GL_TRIANGLES, 0, count);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);

        if(nComps>0) {
            gl.glDisableClientState(GLPointerFunc.GL_NORMAL_ARRAY);
        }
    }
}

long sStartTick = 0;
long sTick = 0;

int sCurrentCamTrack = 0;
long sCurrentCamTrackStartTick = 0;
long sNextCamTrackStartTick = 0x7fffffff;

GLSpatial sSuperShapeObjects[] = new GLSpatial[SuperShape.COUNT];
GLSpatial sGroundPlane;


public class VECTOR3 {
    float x, y, z;

    public VECTOR3() {
        x=0f; y=0f; z=0f;
    }
    public VECTOR3(final float x, final float y, final float z) {
        this.x=x;
        this.y=y;
        this.z=z;
    }
}



static void vector3Sub(final VECTOR3 dest, final VECTOR3 v1, final VECTOR3 v2)
{
    dest.x = v1.x - v2.x;
    dest.y = v1.y - v2.y;
    dest.z = v1.z - v2.z;
}


static void superShapeMap(final VECTOR3 point, final float r1, final float r2, final float t, final float p)
{
    // sphere-mapping of supershape parameters
    point.x = (float)(Math.cos(t) * Math.cos(p) / r1 / r2);
    point.y = (float)(Math.sin(t) * Math.cos(p) / r1 / r2);
    point.z = (float)(Math.sin(p) / r2);
}


float ssFunc(final float t, final float p[])
{
    return ssFunc(t, p, 0);
}

float ssFunc(final float t, final float p[], final int pOff)
{
    return (float)(Math.pow(Math.pow(Math.abs(Math.cos(p[0+pOff] * t / 4)) / p[1+pOff], p[4+pOff]) +
                            Math.pow(Math.abs(Math.sin(p[0+pOff] * t / 4)) / p[2+pOff], p[5+pOff]), 1 / p[3+pOff]));
}


// Creates and returns a supershape object.
// Based on Paul Bourke's POV-Ray implementation.
// http://astronomy.swin.edu.au/~pbourke/povray/supershape/
GLSpatial createSuperShape(final float params[])
{
    final int resol1 = (int)params[SuperShape.PARAMS - 3];
    final int resol2 = (int)params[SuperShape.PARAMS - 2];
    // latitude 0 to pi/2 for no mirrored bottom
    // (latitudeBegin==0 for -pi/2 to pi/2 originally)
    final int latitudeBegin = resol2 / 4;
    final int latitudeEnd = resol2 / 2;    // non-inclusive
    final int longitudeCount = resol1;
    final int latitudeCount = latitudeEnd - latitudeBegin;
    final int triangleCount = longitudeCount * latitudeCount * 2;
    final int vertices = triangleCount * 3;
    GLSpatial result;
    final float baseColor[] = new float[3];
    int a, longitude, latitude;
    int currentVertex, currentQuad;

    result = new GLSpatial(vertices, 3, true);
    if (result == null)
        return null;

    for (a = 0; a < 3; ++a)
        baseColor[a] = ((randomUInt() % 155) + 100) / 255.f;

    currentQuad = 0;
    currentVertex = 0;

    // longitude -pi to pi
    for (longitude = 0; longitude < longitudeCount; ++longitude)
    {

        // latitude 0 to pi/2
        for (latitude = latitudeBegin; latitude < latitudeEnd; ++latitude)
        {
            final float t1 = (float) ( -Math.PI + longitude * 2 * Math.PI / resol1 );
            final float t2 = (float) ( -Math.PI + (longitude + 1) * 2 * Math.PI / resol1 );
            final float p1 = (float) ( -Math.PI / 2 + latitude * 2 * Math.PI / resol2 );
            final float p2 = (float) ( -Math.PI / 2 + (latitude + 1) * 2 * Math.PI / resol2 );
            float r0, r1, r2, r3;

            r0 = ssFunc(t1, params);
            r1 = ssFunc(p1, params, 6);
            r2 = ssFunc(t2, params);
            r3 = ssFunc(p2, params, 6);

            if (r0 != 0 && r1 != 0 && r2 != 0 && r3 != 0)
            {
                final VECTOR3 pa=new VECTOR3(), pb=new VECTOR3(), pc=new VECTOR3(), pd=new VECTOR3();
                final VECTOR3 v1=new VECTOR3(), v2=new VECTOR3(), n=new VECTOR3();
                float ca;
                int i;
                //float lenSq, invLenSq;

                superShapeMap(pa, r0, r1, t1, p1);
                superShapeMap(pb, r2, r1, t2, p1);
                superShapeMap(pc, r2, r3, t2, p2);
                superShapeMap(pd, r0, r3, t1, p2);

                // kludge to set lower edge of the object to fixed level
                if (latitude == latitudeBegin + 1)
                    pa.z = pb.z = 0;

                vector3Sub(v1, pb, pa);
                vector3Sub(v2, pd, pa);

                // Calculate normal with cross product.
                /*   i    j    k      i    j
                 * v1.x v1.y v1.z | v1.x v1.y
                 * v2.x v2.y v2.z | v2.x v2.y
                 */

                n.x = v1.y * v2.z - v1.z * v2.y;
                n.y = v1.z * v2.x - v1.x * v2.z;
                n.z = v1.x * v2.y - v1.y * v2.x;

                /* Pre-normalization of the normals is disabled here because
                 * they will be normalized anyway later due to automatic
                 * normalization (GL2ES1.GL_NORMALIZE). It is enabled because the
                 * objects are scaled with glScale.
                 */
                /*
                lenSq = n.x * n.x + n.y * n.y + n.z * n.z;
                invLenSq = (float)(1 / sqrt(lenSq));
                n.x *= invLenSq;
                n.y *= invLenSq;
                n.z *= invLenSq;
                */

                ca = pa.z + 0.5f;

                if(result.normalArray!=null) {
                    for (i = currentVertex * 3;
                         i < (currentVertex + 6) * 3;
                         i += 3)
                    {
                        result.normalArray.put(i    , (n.x));
                        result.normalArray.put(i + 1, (n.y));
                        result.normalArray.put(i + 2, (n.z));
                    }
                }
                for (i = currentVertex * cComps;
                     i < (currentVertex + 6) * cComps;
                     i += cComps)
                {
                    int j;
                    final float color[] = new float[3];
                    for (j = 0; j < 3; ++j)
                    {
                        color[j] = ca * baseColor[j];
                        if (color[j] > 1.0f) color[j] = 1.0f;
                    }
                    result.colorArray.put(i    , color[0]);
                    result.colorArray.put(i + 1, color[1]);
                    result.colorArray.put(i + 2, color[2]);
                    if(3<cComps) {
                        result.colorArray.put(i + 3, 0f);
                    }
                }
                result.vertexArray.put(currentVertex * 3, (pa.x));
                result.vertexArray.put(currentVertex * 3 + 1, (pa.y));
                result.vertexArray.put(currentVertex * 3 + 2, (pa.z));
                ++currentVertex;
                result.vertexArray.put(currentVertex * 3, (pb.x));
                result.vertexArray.put(currentVertex * 3 + 1, (pb.y));
                result.vertexArray.put(currentVertex * 3 + 2, (pb.z));
                ++currentVertex;
                result.vertexArray.put(currentVertex * 3, (pd.x));
                result.vertexArray.put(currentVertex * 3 + 1, (pd.y));
                result.vertexArray.put(currentVertex * 3 + 2, (pd.z));
                ++currentVertex;
                result.vertexArray.put(currentVertex * 3, (pb.x));
                result.vertexArray.put(currentVertex * 3 + 1, (pb.y));
                result.vertexArray.put(currentVertex * 3 + 2, (pb.z));
                ++currentVertex;
                result.vertexArray.put(currentVertex * 3, (pc.x));
                result.vertexArray.put(currentVertex * 3 + 1, (pc.y));
                result.vertexArray.put(currentVertex * 3 + 2, (pc.z));
                ++currentVertex;
                result.vertexArray.put(currentVertex * 3, (pd.x));
                result.vertexArray.put(currentVertex * 3 + 1, (pd.y));
                result.vertexArray.put(currentVertex * 3 + 2, (pd.z));
                ++currentVertex;
            } // r0 && r1 && r2 && r3
            ++currentQuad;
        } // latitude
    } // longitude

    // Set number of vertices in object to the actual amount created.
    result.setCount(currentVertex);
    result.seal();
    return result;
}


GLSpatial createGroundPlane()
{
    final  int scale = 4;
    final  int yBegin = -15, yEnd = 15;    // ends are non-inclusive
    final  int xBegin = -15, xEnd = 15;
    final  int triangleCount = (yEnd - yBegin) * (xEnd - xBegin) * 2;
    final  int vertices = triangleCount * 3;
    GLSpatial result;
    int x, y;
    int currentVertex, currentQuad;
    final int vcomps = 2;

    result = new GLSpatial(vertices, vcomps, false);
    if (result == null)
        return null;

    currentQuad = 0;
    currentVertex = 0;

    for (y = yBegin; y < yEnd; ++y)
    {
        for (x = xBegin; x < xEnd; ++x)
        {
            float color;
            int i, a;
            color = (randomUInt() % 255)/255.0f;
            for (i = currentVertex * cComps; i < (currentVertex + 6) * cComps; i += cComps)
            {
                result.colorArray.put(i, color);
                result.colorArray.put(i + 1, color);
                result.colorArray.put(i + 2, color);
                if(3<cComps) {
                    result.colorArray.put(i + 3, 0);
                }
            }

            // Axis bits for quad triangles:
            // x: 011100 (0x1c), y: 110001 (0x31)  (clockwise)
            // x: 001110 (0x0e), y: 100011 (0x23)  (counter-clockwise)
            for (a = 0; a < 6; ++a)
            {
                final int xm = x + ((0x1c >> a) & 1);
                final int ym = y + ((0x31 >> a) & 1);
                final float m = (float)(Math.cos(xm * 2) * Math.sin(ym * 4) * 0.75f);
                result.vertexArray.put(currentVertex * vcomps, (xm * scale + m));
                result.vertexArray.put(currentVertex * vcomps + 1, (ym * scale + m));
                if(2<vcomps) {
                    result.vertexArray.put(currentVertex * vcomps + 2, 0f);
                }
                ++currentVertex;
            }
            ++currentQuad;
        }
    }
    result.seal();
    return result;
}


void drawGroundPlane()
{
    gl.glDisable(GLLightingFunc.GL_LIGHTING);
    gl.glDisable(GL.GL_DEPTH_TEST);
    if(blendingEnabled) {
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_ZERO, GL.GL_SRC_COLOR);
    }

    sGroundPlane.draw();

    if(blendingEnabled) {
        gl.glDisable(GL.GL_BLEND);
    }
    gl.glEnable(GL.GL_DEPTH_TEST);
    gl.glEnable(GLLightingFunc.GL_LIGHTING);
}

void drawFadeQuad()
{
    final int beginFade = (int) (sTick - sCurrentCamTrackStartTick);
    final int endFade = (int) (sNextCamTrackStartTick - sTick);
    final int minFade = beginFade < endFade ? beginFade : endFade;

    if (minFade < 1024)
    {
        final float fadeColor = FixedPoint.toFloat(minFade << 7);
        gl.glColor4f(fadeColor, fadeColor, fadeColor, 0f);

        gl.glDisable(GL.GL_DEPTH_TEST);
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_ZERO, GL.GL_SRC_COLOR);
        gl.glDisable(GLLightingFunc.GL_LIGHTING);

        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
        gl.glLoadIdentity();

        gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
        gl.glLoadIdentity();

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
        gl.glDisableClientState(GLPointerFunc.GL_COLOR_ARRAY);
        gl.glDisableClientState(GLPointerFunc.GL_NORMAL_ARRAY);
        gl.glEnableClientState(GLPointerFunc.GL_VERTEX_ARRAY);
        gl.glVertexPointer(2, GL.GL_FLOAT, 0, quadVertices);
        gl.glDrawArrays(GL.GL_TRIANGLES, 0, 6);
        gl.glEnableClientState(GLPointerFunc.GL_COLOR_ARRAY);

        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);

        gl.glEnable(GLLightingFunc.GL_LIGHTING);
        gl.glDisable(GL.GL_BLEND);
        gl.glEnable(GL.GL_DEPTH_TEST);
    }
}

FloatBuffer quadVertices;
FloatBuffer light0Position;
FloatBuffer light0Diffuse;
FloatBuffer light1Position;
FloatBuffer light1Diffuse;
FloatBuffer light2Position;
FloatBuffer light2Diffuse;
FloatBuffer materialSpecular;

void configureLightAndMaterial()
{
    gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_POSITION, light0Position);
    gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_DIFFUSE, light0Diffuse);
    gl.glLightfv(GLLightingFunc.GL_LIGHT1, GLLightingFunc.GL_POSITION, light1Position);
    gl.glLightfv(GLLightingFunc.GL_LIGHT1, GLLightingFunc.GL_DIFFUSE, light1Diffuse);
    gl.glLightfv(GLLightingFunc.GL_LIGHT2, GLLightingFunc.GL_POSITION, light2Position);
    gl.glLightfv(GLLightingFunc.GL_LIGHT2, GLLightingFunc.GL_DIFFUSE, light2Diffuse);
    gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SPECULAR, materialSpecular);

    gl.glMaterialf(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SHININESS, 60.0f);
    gl.glEnable(GLLightingFunc.GL_COLOR_MATERIAL);
}


void drawModels(final float zScale)
{
    final int translationScale = 9;
    int x, y;

    seedRandom(9);

    gl.glScalef(1.0f, 1.0f, zScale);

    for (y = -5; y <= 5; ++y)
    {
        for (x = -5; x <= 5; ++x)
        {
            final int curShape = randomUInt() % SuperShape.COUNT;
            final float buildingScale = SuperShape.sParams[curShape][SuperShape.PARAMS - 1];

            gl.glPushMatrix();
            gl.glTranslatef(x * translationScale,
                            y * translationScale,
                            0f);
            gl.glRotatef(randomUInt() % 360, 0f, 0f, 1f);
            gl.glScalef(buildingScale, buildingScale, buildingScale);

            sSuperShapeObjects[curShape].draw();
            gl.glPopMatrix();
        }
    }

    for (x = -2; x <= 2; ++x)
    {
        final int shipScale100 = translationScale * 500;
        final int offs100 = x * shipScale100 + (int)(sTick % shipScale100);
        final float offs = offs100 * 0.01f;
        gl.glPushMatrix();
        gl.glTranslatef(offs, -4.0f, 2.0f);
        sSuperShapeObjects[SuperShape.COUNT - 1].draw();
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslatef(-4.0f, offs, 4.0f);
        gl.glRotatef(90.0f, 0.0f, 0.0f, 1.0f);
        sSuperShapeObjects[SuperShape.COUNT - 1].draw();
        gl.glPopMatrix();
    }
}


void camTrack()
{
    final float lerp[]= new float[5];
    float eX, eY, eZ, cX, cY, cZ;
    float trackPos;
    CamTrack cam;
    long currentCamTick;
    int a;

    if (sNextCamTrackStartTick <= sTick)
    {
        ++sCurrentCamTrack;
        sCurrentCamTrackStartTick = sNextCamTrackStartTick;
    }
    sNextCamTrackStartTick = sCurrentCamTrackStartTick +
                             CamTrack.sCamTracks[sCurrentCamTrack].len * CamTrack.CAMTRACK_LEN;

    cam = CamTrack.sCamTracks[sCurrentCamTrack];
    currentCamTick = sTick - sCurrentCamTrackStartTick;
    trackPos = (float)currentCamTick / (CamTrack.CAMTRACK_LEN * cam.len);

    for (a = 0; a < 5; ++a)
        lerp[a] = (cam.src[a] + cam.dest[a] * trackPos) * 0.01f;

    if (cam.dist>0)
    {
        final float dist = cam.dist * 0.1f;
        cX = lerp[0];
        cY = lerp[1];
        cZ = lerp[2];
        eX = cX - (float)Math.cos(lerp[3]) * dist;
        eY = cY - (float)Math.sin(lerp[3]) * dist;
        eZ = cZ - lerp[4];
    }
    else
    {
        eX = lerp[0];
        eY = lerp[1];
        eZ = lerp[2];
        cX = eX + (float)Math.cos(lerp[3]);
        cY = eY + (float)Math.sin(lerp[3]);
        cZ = eZ + lerp[4];
    }
    glu.gluLookAt(eX, eY, eZ, cX, cY, cZ, 0, 0, 1);
}

private int gAppAlive = 0;
private int width, height, x, y, frames;
}

