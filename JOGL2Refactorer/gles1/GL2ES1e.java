/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
import java.nio.Buffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES1;

/**
 * Interface containing all the GL2 constants and methods missing from GLES1
 * that are required by the World Wind Java SDK.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface GL2ES1e extends GL2ES1
{
	//glPushAttrib bits
	int GL_CURRENT_BIT = GL2.GL_CURRENT_BIT;
	int GL_LINE_BIT = GL2.GL_LINE_BIT;
	int GL_TRANSFORM_BIT = GL2.GL_TRANSFORM_BIT;
	int GL_ENABLE_BIT = GL2.GL_ENABLE_BIT;
	int GL_POLYGON_BIT = GL2.GL_POLYGON_BIT;
	int GL_VIEWPORT_BIT = GL2.GL_VIEWPORT_BIT;
	int GL_LIGHTING_BIT = GL2.GL_LIGHTING_BIT;
	int GL_HINT_BIT = GL2.GL_HINT_BIT;
	int GL_POINT_BIT = GL2.GL_POINT_BIT;
	int GL_SCISSOR_BIT = GL2.GL_SCISSOR_BIT;

	//glPushClientAttrib bits
	int GL_CLIENT_VERTEX_ARRAY_BIT = GL2.GL_CLIENT_VERTEX_ARRAY_BIT;

	//display lists
	int GL_COMPILE = GL2.GL_COMPILE;

	//polygon mode
	int GL_FILL = GL2.GL_FILL;
	int GL_LINE = GL2.GL_LINE;
	int GL_POINT = GL2.GL_POINT;

	//glBegin constants
	int GL_POLYGON = GL2.GL_POLYGON;
	int GL_QUAD_STRIP = GL2.GL_QUAD_STRIP;
	int GL_QUADS = GL2.GL_QUADS;

	//hints
	int GL_POLYGON_SMOOTH_HINT = GL2.GL_POLYGON_SMOOTH_HINT;

	//data types
	int GL_INT = GL2.GL_INT;
	int GL_DOUBLE = GL2.GL_DOUBLE;

	//interleaved array
	int GL_C3F_V3F = GL2.GL_C3F_V3F;

	//lighting model
	int GL_LIGHT_MODEL_LOCAL_VIEWER = GL2.GL_LIGHT_MODEL_LOCAL_VIEWER;

	//line stippling
	int GL_LINE_STIPPLE = GL2.GL_LINE_STIPPLE;

	//hardware attributes
	int GL_MAX_TEXTURE_IMAGE_UNITS = GL2.GL_MAX_TEXTURE_IMAGE_UNITS;
	int GL_MAX_TEXTURE_COORDS = GL2.GL_MAX_TEXTURE_COORDS;
	int GL_MAX_ELEMENTS_INDICES = GL2.GL_MAX_ELEMENTS_INDICES;
	int GL_MAX_ELEMENTS_VERTICES = GL2.GL_MAX_ELEMENTS_VERTICES;

	//framebuffer statuses
	int GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER = GL2.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER;
	int GL_FRAMEBUFFER_INCOMPLETE_LAYER_COUNT_EXT = GL2.GL_FRAMEBUFFER_INCOMPLETE_LAYER_COUNT_EXT;
	int GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS_EXT = GL2.GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS_EXT;
	int GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE = GL2.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE;
	int GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER = GL2.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER;

	//VBO buffer usage
	int GL_STREAM_DRAW = GL2.GL_STREAM_DRAW;

	//glDrawBuffer arguments
	int GL_BACK_LEFT = GL2.GL_BACK_LEFT;
	int GL_BACK_RIGHT = GL2.GL_BACK_RIGHT;

	//texture coordinate properties
	int GL_CLAMP_TO_BORDER = GL2.GL_CLAMP_TO_BORDER;

	//texture coordinate generation
	int GL_S = GL2.GL_S;
	int GL_T = GL2.GL_T;
	int GL_TEXTURE_GEN_S = GL2.GL_TEXTURE_GEN_S;
	int GL_TEXTURE_GEN_T = GL2.GL_TEXTURE_GEN_T;
	int GL_EYE_LINEAR = GL2.GL_EYE_LINEAR;
	int GL_OBJECT_LINEAR = GL2.GL_OBJECT_LINEAR;
	int GL_OBJECT_PLANE = GL2.GL_OBJECT_PLANE;

	//texture pixel formats
	int GL_RED = GL2.GL_RED;

	//texture formats
	int GL_ALPHA4 = GL2.GL_ALPHA4;
	int GL_ALPHA8 = GL2.GL_ALPHA8;
	int GL_LUMINANCE4 = GL2.GL_LUMINANCE4;
	int GL_INTENSITY4 = GL2.GL_INTENSITY4;
	int GL_LUMINANCE8 = GL2.GL_LUMINANCE8;
	int GL_LUMINANCE4_ALPHA4 = GL2.GL_LUMINANCE4_ALPHA4;
	int GL_LUMINANCE6_ALPHA2 = GL2.GL_LUMINANCE6_ALPHA2;
	int GL_INTENSITY = GL2.GL_INTENSITY;
	int GL_INTENSITY8 = GL2.GL_INTENSITY8;
	int GL_R3_G3_B2 = GL2.GL_R3_G3_B2;
	int GL_RGBA2 = GL2.GL_RGBA2;
	int GL_SLUMINANCE = GL2.GL_SLUMINANCE;
	int GL_SLUMINANCE8 = GL2.GL_SLUMINANCE8;
	int GL_ALPHA12 = GL2.GL_ALPHA12;
	int GL_LUMINANCE12 = GL2.GL_LUMINANCE12;
	int GL_INTENSITY12 = GL2.GL_INTENSITY12;
	int GL_RGB4 = GL2.GL_RGB4;
	int GL_ALPHA16 = GL2.GL_ALPHA16;
	int GL_LUMINANCE16 = GL2.GL_LUMINANCE16;
	int GL_LUMINANCE8_ALPHA8 = GL2.GL_LUMINANCE8_ALPHA8;
	int GL_LUMINANCE12_ALPHA4 = GL2.GL_LUMINANCE12_ALPHA4;
	int GL_INTENSITY16 = GL2.GL_INTENSITY16;
	int GL_RGB5 = GL2.GL_RGB5;
	int GL_SLUMINANCE_ALPHA = GL2.GL_SLUMINANCE_ALPHA;
	int GL_SLUMINANCE8_ALPHA8 = GL2.GL_SLUMINANCE8_ALPHA8;
	int GL_DEPTH_COMPONENT = GL2.GL_DEPTH_COMPONENT;
	int GL_LUMINANCE12_ALPHA12 = GL2.GL_LUMINANCE12_ALPHA12;
	int GL_SRGB8 = GL2.GL_SRGB8;
	int GL_LUMINANCE16_ALPHA16 = GL2.GL_LUMINANCE16_ALPHA16;
	int GL_RGB12 = GL2.GL_RGB12;
	int GL_RGB16 = GL2.GL_RGB16;
	int GL_RGBA12 = GL2.GL_RGBA12;
	int GL_RGBA16 = GL2.GL_RGBA16;
	int GL_COMPRESSED_ALPHA = GL2.GL_COMPRESSED_ALPHA;
	int GL_COMPRESSED_LUMINANCE = GL2.GL_COMPRESSED_LUMINANCE;
	int GL_COMPRESSED_LUMINANCE_ALPHA = GL2.GL_COMPRESSED_LUMINANCE_ALPHA;
	int GL_COMPRESSED_INTENSITY = GL2.GL_COMPRESSED_INTENSITY;
	int GL_COMPRESSED_RGB = GL2.GL_COMPRESSED_RGB;
	int GL_COMPRESSED_RGBA = GL2.GL_COMPRESSED_RGBA;

	//display lists

	int glGenLists(int range);

	void glNewList(int list, int mode);

	void glDeleteLists(int list, int range);

	void glEndList();

	void glCallList(int list);

	//double matrix math

	void glMultMatrixd(double[] m, int m_offset);

	void glTranslated(double x, double y, double z);

	void glScaled(double x, double y, double z);

	void glRotated(double angle, double x, double y, double z);

	void glLoadMatrixd(double[] m, int m_offset);

	//primitive drawing

	void glBegin(int mode);

	void glEnd();

	void glVertex2i(int x, int y);

	void glVertex2f(float x, float y);

	void glVertex2d(double x, double y);

	void glVertex3f(float x, float y, float z);

	void glVertex3d(double x, double y, double z);

	void glVertex3dv(double[] v, int v_offset);

	void glColor3ub(byte red, byte green, byte blue);

	void glColor3f(float red, float green, float blue);

	void glColor3d(double red, double green, double blue);

	void glColor4d(double red, double green, double blue, double alpha);

	void glColor4fv(float[] v, int v_offset);

	void glTexCoord2d(double s, double t);

	void glRecti(int x1, int y1, int x2, int y2);

	void glRectf(float x1, float y1, float x2, float y2);

	void glRectd(double x1, double y1, double x2, double y2);

	//state stack

	void glPushAttrib(int mask);

	void glPopAttrib();

	void glPushClientAttrib(int mask);

	void glPopClientAttrib();

	//state quering

	void glGetDoublev(int pname, double[] params, int params_offset);

	//draw mode

	void glPolygonMode(int face, int mode);

	//interleaved arrays

	void glInterleavedArrays(int format, int stride, Buffer pointer);

	void glInterleavedArrays(int format, int stride, long pointer_buffer_offset);

	void glDrawRangeElements(int mode, int start, int end, int count, int type, Buffer indices);

	void glMultiDrawArrays(int mode, IntBuffer first, IntBuffer count, int primcount);

	//line stippling

	void glLineStipple(int factor, short pattern);

	//texture coordinate generation

	void glTexGendv(int coord, int pname, double[] params, int params_offset);

	//light model

	void glLightModeli(int pname, int param);

	void glColorMaterial(int face, int mode);

	//draw buffer selection

	void glDrawBuffer(int mode);
}
