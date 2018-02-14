/*
 * Copyright (c) 2013 - 2016 Stefan Muller Arisona, Simon Schubiger
 * Copyright (c) 2013 - 2016 FHNW & ETH Zurich
 * All rights reserved.
 *
 * Contributions by: Filip Schramka, Samuel von Stachelski
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *  Neither the name of FHNW / ETH Zurich nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ch.fhnw.ether.formats.obj;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ch.fhnw.ether.formats.ModelFace;
import ch.fhnw.ether.formats.ModelGroup;
import ch.fhnw.ether.formats.ModelObject;
import ch.fhnw.ether.platform.IImageSupport;
import ch.fhnw.ether.platform.Platform;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.IMesh.Primitive;
import ch.fhnw.ether.scene.mesh.geometry.IGeometry;
import ch.fhnw.ether.scene.mesh.geometry.IGeometry.IGeometryAttribute;
import ch.fhnw.ether.scene.mesh.material.ColorMapMaterial;
import ch.fhnw.ether.scene.mesh.material.ColorMaterial;
import ch.fhnw.ether.scene.mesh.material.IMaterial;
import ch.fhnw.ether.scene.mesh.material.ShadedMaterial;
import ch.fhnw.util.Log;
import ch.fhnw.util.Log.Level;
import ch.fhnw.util.TextUtilities;
import ch.fhnw.util.math.Vec2;
import ch.fhnw.util.math.Vec3;

public final class ObjWriter {
	private static final Log  log = Log.create(Level.SEVERE);

	private final ModelObject object;
	private final PrintWriter out;
	private final PrintWriter mtl;
	private final Set<String> materials = new HashSet<>();
	private final File        basePath;

	public ObjWriter(File file) throws FileNotFoundException {
		URL resource = null;
		try {
			resource = file.toURI().toURL();
		} catch (Exception e) {
		}
		basePath = file.getParentFile();
		object   = new ModelObject(resource);
		out      = new PrintWriter(file);
		String mtlName = TextUtilities.getFileNameWithoutExtension(file) + ".mtl";
		mtl      = new PrintWriter(new File(basePath, mtlName));
		out.println("mtllib " + mtlName);
	}

	int vidx = 0;
	public void addMesh(IMesh mesh) {		
		if (mesh.getType() != Primitive.TRIANGLES)
			return;

		ModelGroup g = new ModelGroup(mesh.getMaterial().getName());

		if(materials.add(mesh.getMaterial().getName()))
			writeMaterial(mesh.getMaterial());

		float[][] gdata = mesh.getTransformedGeometryData();

		int[] vi = new int[3];
		int[] ni = new int[3];
		int[] ti = new int[3];

		final List<Vec3> vs = object.getVertices();
		final List<Vec3> ns = object.getNormals();
		final List<Vec2> ts = object.getTexCoords();

		IGeometryAttribute[] attrs = mesh.getGeometry().getAttributes();

		int vc = 0;
		int nc = 0;
		int tc = 0;
		for (int i = 0; i < gdata[0].length; i += 9) {
			boolean hasNormals = false;
			boolean hasUVs     = false;
			for(int a = 0; a < attrs.length; a++) {
				float[] data = gdata[a];
				if(attrs[a].equals(IGeometry.POSITION_ARRAY)) {
					vs.add(new Vec3(data[i + 0], data[i + 1], data[i + 2]));
					vi[0] = vs.size();
					vs.add(new Vec3(data[i + 3], data[i + 4], data[i + 5]));
					vi[1] = vs.size();
					vs.add(new Vec3(data[i + 6], data[i + 7], data[i + 8]));
					vi[2] = vs.size();
					vc += 3;
				} else if(attrs[a].equals(IGeometry.NORMAL_ARRAY)) {
					ns.add(new Vec3(data[i + 0], data[i + 1], data[i + 2]));
					ni[0] = ns.size();
					ns.add(new Vec3(data[i + 3], data[i + 4], data[i + 5]));
					ni[1] = ns.size();
					ns.add(new Vec3(data[i + 6], data[i + 7], data[i + 8]));
					ni[2] = ns.size();
					hasNormals = true;
					nc += 3;
				} else if(attrs[a].equals(IGeometry.COLOR_MAP_ARRAY)) {
					int it = (6 * i) / 9;
					ts.add(new Vec2(data[it + 0], data[it + 1]));
					ti[0] = ts.size();
					ts.add(new Vec2(data[it + 2], data[it + 3]));
					ti[1] = ts.size();
					ts.add(new Vec2(data[it + 4], data[it + 5]));
					ti[2] = ts.size();
					hasUVs = true;
					tc += 3;
				}
			}					
			g.addFace(new ModelFace(vi.clone(), hasNormals ? ni.clone() : null, hasUVs ? ti.clone() : null));
		}
		addGroup(g, vc, nc, tc);
	}

	private void writeMaterial(IMaterial material) {
		mtl.println("newmtl Material_" + material.getName());
		if(material instanceof ColorMapMaterial || material instanceof ShadedMaterial) {
			mtl.println("illum 4");
			mtl.println("Ka 0.00 0.00 0.00");
			mtl.println("Kd 1.00 1.00 1.00");
			mtl.println("Ks 0.00 0.00 0.00");
			mtl.println("Tf 1.00 1.00 1.00");
			mtl.println("Ni 1.00");
			mtl.println("map_Kd " + material.getName() + ".png");
			mtl.println();
			if(material instanceof ShadedMaterial) {
				try {
					Platform.get().getImageSupport().write(((ShadedMaterial)material).getColorMap(), 
							new FileOutputStream(new File(basePath, material.getName() + ".png")), 
							IImageSupport.FileFormat.PNG);
				} catch(Throwable t) {
					log.severe(t);
				}
			} else if(material instanceof ColorMapMaterial) {
				try {
					Platform.get().getImageSupport().write(((ColorMapMaterial)material).getColorMap(), 
							new FileOutputStream(new File(basePath, material.getName() + ".png")), 
							IImageSupport.FileFormat.PNG);
				} catch(Throwable t) {
					log.severe(t);
				}
			}
		} else if(material instanceof ColorMaterial) {
			//.... add color material support
		}
	}

	public void addV(Vec3 vec) {
		object.getVertices().add(vec);
	}

	public void addN(Vec3 vec) {
		object.getNormals().add(vec);
	}

	public void addGroup(ModelGroup g, int vertexCount, int normalCount, int uvCount) {
		g.setCounts(vertexCount, normalCount, uvCount);
		object.addGroup(g);
	}

	public void write() {
		Iterator<Vec3> vi = object.getVertices().iterator();
		Iterator<Vec3> ni = object.getNormals().iterator();
		Iterator<Vec2> ti = object.getTexCoords().iterator();
		
		for (ModelGroup g : object.getGroups()) {
			out.println();
			out.println("o " + g.getName());
			int vc = g.getVertexCount();
			int nc = g.getNormalCount();
			int tc = g.getUVCount();
			
			for (int i = 0; i < vc; i++) {
				Vec3 v = vi.next();
				out.println("v " + v.x + " " + v.y + " " + v.z);
			}
			for (int i = 0; i < tc; i++) {
				Vec2 t = ti.next();
				out.println("vt " + t.x + " " + t.y);				
			}
			for (int i = 0; i < nc; i++) {
				Vec3 n = ni.next();
				out.println("vn " + n.x + " " + n.y + " " + n.z);				
			}

			out.println("usemtl Material_" + g.getName());
			for (ModelFace f : g.getFaces())
				writeFace(f, out);
		}
		out.close();
		mtl.close();
	}

	private void writeFace(ModelFace face, PrintWriter out) {
		StringBuilder result = new StringBuilder();
		result.append("f ");
		int[] vIndices = face.getVertexIndices();
		int[] nIndices = face.getNormalIndices();
		int[] tIndices = face.getTexCoordIndices();
		for (int i = 0; i < vIndices.length; i++) {
			result.append(vIndices[i]);
			if (tIndices != null) {
				result.append('/');
				result.append(tIndices[i]);
			}
			if (nIndices != null) {
				result.append(tIndices == null ? "//" : "/");
				result.append(nIndices[i]);
			}
			result.append(' ');
		}
		out.println(result.toString());
	}
}
