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

package ch.fhnw.ether.formats;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import ch.fhnw.ether.scene.mesh.DefaultMesh;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.IMesh.Flag;
import ch.fhnw.ether.scene.mesh.IMesh.Primitive;
import ch.fhnw.ether.scene.mesh.geometry.DefaultGeometry;
import ch.fhnw.ether.scene.mesh.geometry.IGeometry;
import ch.fhnw.ether.scene.mesh.material.IMaterial;
import ch.fhnw.ether.scene.mesh.material.ShadedMaterial;
import ch.fhnw.util.IntList;
import ch.fhnw.util.TextUtilities;
import ch.fhnw.util.color.RGB;
import ch.fhnw.util.math.Vec2;
import ch.fhnw.util.math.Vec3;
import ch.fhnw.util.math.geometry.BoundingBox;
import ch.fhnw.util.math.tessellator.Triangulation;

public final class ModelObject {
	private final URL resource;

	private final List<Vec3> vertices = new ArrayList<>();
	private final List<Vec3> normals = new ArrayList<>();
	private final List<Vec2> texCoords = new ArrayList<>();

	private final List<ModelGroup> groups = new ArrayList<>();

	private Map<String, ModelMaterial> materials = new HashMap<>();

	private BoundingBox bounds;

	public ModelObject(URL resource) {
		this.resource = resource;
	}

	public URL getResource() {
		return resource;
	}

	public List<Vec3> getVertices() {
		return vertices;
	}

	public List<Vec3> getNormals() {
		return normals;
	}

	public List<Vec2> getTexCoords() {
		return texCoords;
	}

	public List<ModelGroup> getGroups() {
		return groups;
	}

	public Map<String, ModelMaterial> getMaterials() {
		return materials;
	}

	public BoundingBox getBounds() {
		if (bounds == null) {
			bounds = new BoundingBox();
			bounds.add(getVertices());
		}
		return bounds;
	}

	public List<List<Vec3>> getExpandedVertices() {
		List<List<Vec3>> vv = new ArrayList<>();
		for (ModelGroup g : groups) {
			for (ModelFace f : g.getFaces()) {
				List<Vec3> v = new ArrayList<>();
				for (int i : f.getVertexIndices()) {
					v.add(vertices.get(i));
				}
				vv.add(v);
			}
		}
		return vv;
	}

	public List<List<Vec3>> getExpandedNormals() {
		List<List<Vec3>> vv = new ArrayList<>();
		for (ModelGroup g : groups) {
			for (ModelFace f : g.getFaces()) {
				List<Vec3> v = new ArrayList<>();
				for (int i : f.getNormalIndices()) {
					v.add(normals.get(i));
				}
				vv.add(v);
			}
		}
		return vv;
	}

	public List<List<Vec2>> getExpandedTexCoords() {
		List<List<Vec2>> vv = new ArrayList<>();
		for (ModelGroup g : groups) {
			for (ModelFace f : g.getFaces()) {
				List<Vec2> v = new ArrayList<>();
				for (int i : f.getTexCoordIndices()) {
					v.add(texCoords.get(i));
				}
				vv.add(v);
			}
		}
		return vv;		
	}

	public List<IMesh> getMeshes() {
		return getMeshes(null);
	}

	public List<IMesh> getMeshes(IMaterial requestedMaterial) {
		final List<IMesh> meshes = new ArrayList<>();

		List<Vec3> vertices = getVertices();
		List<Vec3> normals = getNormals();
		List<Vec2> texCoords = getTexCoords();

		Map<ModelMaterial, IMaterial> materials = new IdentityHashMap<>();

		for (ModelGroup group : getGroups()) {
			List<ModelFace> faces = group.getFaces();
			if (faces.isEmpty())
				continue;
			boolean hasNormals   = faces.get(0).getNormalIndices() != null;
			boolean hasTexCoords = faces.get(0).getTexCoordIndices() != null;

			List<Vec3> triVertices = new ArrayList<>();
			List<Vec3> triNormals = hasNormals ? new ArrayList<>() : null;
			List<Vec2> triTexCoords = hasTexCoords ? new ArrayList<>() : null;

			for (ModelFace face : group.getFaces()) {
				final int[] vs   = face.getVertexIndices();
				final int[] ns   = face.getNormalIndices();
				final int[] ts   = face.getTexCoordIndices();
				final int[] nmap; 
				final int[] tmap; 

				if(vs.length == 3) {
					for (int idx = 0; idx < vs.length; idx++) {
						triVertices.add(vertices.get(vs[idx]));
						if (hasNormals)
							triNormals.add(ns != null ? normals.get(ns[idx]) : Vec3.Z);
						if (hasTexCoords)
							triTexCoords.add(ts != null ? texCoords.get(ts[idx]) : Vec2.ZERO);
					}
				} else {
					List<Vec3> polyVertices = new ArrayList<>();
					for (int i = 0; i < vs.length; ++i)
						polyVertices.add(vertices.get(vs[i]));

					if(ns != null) {
						nmap = new int[vs.length]; 
						for (int i = 0; i < vs.length; ++i)
							nmap[i] = ns[i];
					} else nmap = null;

					if(ts != null) {
						tmap = new int[vs.length]; 
						for (int i = 0; i < vs.length; ++i)
							tmap[i] = ts[i];
					} else tmap = null;

					IntList triangulation = Triangulation.triangulate(Vec3.toArray(polyVertices));

					for (int i = 0; i < triangulation.size(); ++i) {
						int idx = triangulation.get(i);
						triVertices.add(vertices.get(vs[idx]));
						if (hasNormals)
							triNormals.add(ns != null ? normals.get(nmap[idx]) : Vec3.Z);
						if (hasTexCoords)
							triTexCoords.add(ts != null ? texCoords.get(tmap[idx]) : Vec2.ZERO);
					}
				}
			}

			// TODO: improve material handling
			IMaterial material;
			if (requestedMaterial != null) {
				material = requestedMaterial;
			} else {
				material = group.getMaterial(materials);
			}

			float[] tv = Vec3.toArray(triVertices);
			float[] tn = Vec3.toArray(triNormals);
			float[] tt = Vec2.toArray(triTexCoords);

			IGeometry geometry;
			if (hasTexCoords)
				geometry = DefaultGeometry.createVNM(tv, tn, tt);
			else
				geometry = DefaultGeometry.createVN(tv, tn);

			DefaultMesh mesh = new DefaultMesh(Primitive.TRIANGLES, material, geometry, Flag.DONT_CULL_FACE);
			String filename = TextUtilities.getFileName(getResource().getFile());
			mesh.setName(filename + '/' + group.getName());
			meshes.add(mesh);
		}
		return meshes;		
	}
	
	public void addGroup(ModelGroup g) {
		groups.add(g);
	}
}
