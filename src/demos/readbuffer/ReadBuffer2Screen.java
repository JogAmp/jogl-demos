/*
 * Copyright (c) 2008 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN
 * MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR
 * ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR
 * DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
 * DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF
 * SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 */

package demos.readbuffer;

import java.nio.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.fixedfunc.*;

import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.texture.TextureCoords;
import com.jogamp.opengl.util.GLArrayDataClient;
import com.jogamp.opengl.util.GLArrayDataServer;

public class ReadBuffer2Screen extends ReadBufferBase {
    PMVMatrix pmvMatrix;
    GLArrayDataClient readTextureVertices = null;
    GLArrayDataClient readTextureCoords = null;
    boolean enableBufferAlways = false; // FIXME
    boolean enableBufferVBO    = true; // FIXME

    public ReadBuffer2Screen (final GLDrawable externalRead) {
        super(externalRead);
    }

    @Override
    public void init(final GLAutoDrawable drawable) {
        super.init(drawable);

        final GL gl = drawable.getGL();

        pmvMatrix = new PMVMatrix();

        final float f_edge = 1f;
        if(null==readTextureVertices) {
            //readTextureVertices = GLArrayDataClient.createFixed(gl, GLPointerFunc.GL_VERTEX_ARRAY,
            //                                                    2, GL.GL_FLOAT, true, 4);
            readTextureVertices = GLArrayDataServer.createFixed(GLPointerFunc.GL_VERTEX_ARRAY,
                                                                2, GL.GL_FLOAT, true, 4, GL.GL_STATIC_DRAW);
            readTextureVertices.setEnableAlways(enableBufferAlways);
            readTextureVertices.setVBOEnabled(enableBufferVBO);
            {
                final FloatBuffer vb = (FloatBuffer)readTextureVertices.getBuffer();
                vb.put(-f_edge); vb.put(-f_edge);
                vb.put( f_edge); vb.put(-f_edge);
                vb.put(-f_edge); vb.put( f_edge);
                vb.put( f_edge); vb.put( f_edge);
            }
            readTextureVertices.seal(gl, true);
            System.out.println(readTextureVertices);
        }

        // Clear background to gray
        gl.glClearColor(0.5f, 0.5f, 0.5f, 0.4f);
    }

    @Override
    public void reshape(final GLAutoDrawable drawable, final int x, final int y, final int width, final int height) {
        super.reshape(drawable, x, y, width, height);

        final GL gl = drawable.getGL();

        gl.glViewport(0, 0, width, height);

        if(gl instanceof GLLightingFunc) {
            ((GLLightingFunc)gl).glShadeModel(GLLightingFunc.GL_SMOOTH);
        }

        final float[] f16 = new float[16];
        GLMatrixFunc glM;
        if(gl instanceof GLMatrixFunc) {
            glM = (GLMatrixFunc)gl;
        } else {
            throw new GLException("ES2 currently unhandled .. ");
        }

        // Identity ..
        pmvMatrix.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
        pmvMatrix.glLoadIdentity();
        pmvMatrix.glTranslatef(0, 0, -2.5f);
        if(null!=glM) {
            glM.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
            glM.glLoadMatrixf(pmvMatrix.getMv().get(f16), 0);
        }

        // Set location in front of camera
        pmvMatrix.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
        pmvMatrix.glLoadIdentity();
        pmvMatrix.gluPerspective(45.0f, (float)width / (float)height, 1.0f, 100.0f);
        if(null!=glM) {
            glM.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
            glM.glLoadMatrixf(pmvMatrix.getP().get(f16), 0);
        }
    }

    @Override
    public void dispose(final GLAutoDrawable drawable) {
        super.dispose(drawable);
    }

    void renderOffscreenTexture(final GL gl) {
      if(!readBufferUtil.isValid()) return;

      // Now draw one quad with the texture
      readBufferUtil.getTexture().enable(gl);
      readBufferUtil.getTexture().bind(gl);

      if(gl.isGL2ES1()) {
          // gl.getGL2ES1().glTexEnvi(GL2ES1.GL_TEXTURE_ENV, GL2ES1.GL_TEXTURE_ENV_MODE, GL2ES1.GL_REPLACE);
          gl.getGL2ES1().glTexEnvi(GL2ES1.GL_TEXTURE_ENV, GL2ES1.GL_TEXTURE_ENV_MODE, GL2ES1.GL_MODULATE);
      }

      updateTextureCoords(gl, false);

      readTextureVertices.enableBuffer(gl, true);
      if(null!=readTextureCoords) {
          readTextureCoords.enableBuffer(gl, true);
      }
      gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, readTextureVertices.getElemCount());
      /**
      if(null!=readTextureCoords) {
          readTextureCoords.enableBuffer(gl, false);
      }
      readTextureVertices.enableBuffer(gl, false); */

      readBufferUtil.getTexture().disable(gl);
    }

    void updateTextureCoords(final GL gl, final boolean force) {
        if(force || null==readTextureCoords) {
            readTextureCoords = GLArrayDataServer.createFixed(GLPointerFunc.GL_TEXTURE_COORD_ARRAY,
                                                              2, GL.GL_FLOAT, true, 4, GL.GL_STATIC_DRAW);
            readTextureCoords.setEnableAlways(enableBufferAlways);
            readTextureCoords.setVBOEnabled(enableBufferVBO);
            {
                final TextureCoords coords = readBufferUtil.getTexture().getImageTexCoords();
                final FloatBuffer cb = (FloatBuffer)readTextureCoords.getBuffer();
                cb.put(coords.left());  cb.put(coords.bottom());
                cb.put(coords.right()); cb.put(coords.bottom());
                cb.put(coords.left());  cb.put(coords.top());
                cb.put(coords.right()); cb.put(coords.top());
            }
            readTextureCoords.seal(gl, true);
            System.out.println(readTextureCoords);
        }
    }

    @Override
    public void display(final GLAutoDrawable drawable) {
        super.display(drawable);

        final GL gl = drawable.getGL();

        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        if(gl instanceof GLLightingFunc) {
            ((GLLightingFunc)gl).glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        }

        renderOffscreenTexture(gl);
    }
}

