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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import ch.fhnw.ether.formats.ModelFace;
import ch.fhnw.ether.formats.ModelGroup;
import ch.fhnw.ether.formats.ModelObject;
import ch.fhnw.util.ArrayUtilities;
import ch.fhnw.util.IntList;
import ch.fhnw.util.Log;
import ch.fhnw.util.TextUtilities;
import ch.fhnw.util.math.Vec2;
import ch.fhnw.util.math.Vec3;

final class ObjParser {
	private static final Log log = Log.create();
	
	private final ModelObject object;
	private final boolean convertToZUp;

	private final String path;

	private ModelGroup currentGroup;

	public ObjParser(ModelObject object, boolean convertToZUp) {
		this.object = object;
		this.convertToZUp = convertToZUp;
		
		// obtain context path (for mtl and textures)
		String file = object.getResource().getFile();
		String p = "";
		int lastSlashIndex = file.lastIndexOf('/');
		if (lastSlashIndex != -1)
			p = file.substring(0, lastSlashIndex + 1);

		lastSlashIndex = file.lastIndexOf('\\');
		if (lastSlashIndex != -1)
			p = file.substring(0, lastSlashIndex + 1);
		this.path = p;
	}
	
	public void parse() throws IOException {
		parse(object.getResource().openStream());
	}

	private void parse(InputStream input) {
		int line = 1;
		try (BufferedReader in = new BufferedReader(new InputStreamReader(input))) {
			for (String string = null; (string = in.readLine()) != null;) {
				parseLine(string, line);
				line++;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error reading OBJ file:'" + object.getResource() + "' (line " + line + ")");
		}
	}

	private void parseLine(String string, int line) {
		if ("".equals(string))
			return;

		String[] words = TextUtilities.tokens(string);

		for(int i = 0; i < words.length; i++)
			if(words[i].startsWith("#"))
				words = ArrayUtilities.keep(words, i);
		
		if (words.length == 0)
			return;
		
		switch (words[0]) {
		case "v":
			parseVertex(words);
			break;
		case "vn":
			parseNormal(words);
			break;
		case "vt":
			parseTexCoord(words);
			break;
		case "f":
			parseFace(words);
			break;
		case "mtllib":
			parseMtllib(words);
			break;
		case "usemtl":
			parseUsemtl(words);
			break;
		case "g":
		case "o":
			parseGroup(words);
			break;
		case "s":
			// TODO: we silently ignore smoothing groups
			break;
		case "l":
			// TODO: we silently ignore lines
			break;
		default:
			log.warning("Ignoring unknown OBJ key '" + words[0] + "' (line " + line + ")");
		}
	}

	private static String[] expand(String[] words, int count) {
		if(words.length >= count) return words;
		String[] result = Arrays.copyOf(words, count);
		for(int i = words.length; i < result.length; i++) result[i] = "0";
		return result;
	}

	private void parseVertex(String[] words) {
		words = expand(words, 4);
		float x = Float.parseFloat(words[1]);
		float y = convertToZUp ? -Float.parseFloat(words[3]) : Float.parseFloat(words[2]);
		float z = convertToZUp ? Float.parseFloat(words[2]) : Float.parseFloat(words[3]);
		object.getVertices().add(new Vec3(x, y , z));
	}

	private void parseNormal(String[] words) {
		words = expand(words, 4);
		float x = Float.parseFloat(words[1]);
		float y = convertToZUp ? -Float.parseFloat(words[3]) : Float.parseFloat(words[2]);
		float z = convertToZUp ? Float.parseFloat(words[2]) : Float.parseFloat(words[3]);
		object.getNormals().add(new Vec3(x, y , z));
	}

	private void parseTexCoord(String[] words) {
		words = expand(words, 3);
		float u = 0;
		float v = 0;
		if (words.length > 1)
			u = Float.parseFloat(words[1]);
		if (words.length > 2)
			v = Float.parseFloat(words[2]);
		object.getTexCoords().add(new Vec2(u, v));
	}

	private void parseFace(String[] words) {
		int numVertices = words.length - 1;
		int[]   vIndices = new int[numVertices];
		IntList nIndices = new IntList(numVertices);
		IntList tIndices = new IntList(numVertices);

		int currentTexCoord;
		for (int i = 0; i < numVertices; ++i) {
			String[] indices = words[i + 1].split("/");

			// vertex
			vIndices[i] = Integer.parseInt(indices[0]) - 1;
			if (vIndices[i] < 0)
				vIndices[i] = object.getVertices().size() + vIndices[i] + 1;

			if (indices.length == 1)
				continue;

			// texcoord
			if (!indices[1].equals("")) {
				currentTexCoord = Integer.parseInt(indices[1]);
				// sometimes '1' is put instead blank if there are no texcoords
				// (v/1/n instead of v//n or v/n)
				if (currentTexCoord <= object.getTexCoords().size()) {
					tIndices.add(currentTexCoord - 1);
				}
				if (tIndices.get(i) < 0)
					tIndices.set(i, object.getTexCoords().size() + tIndices.get(i) + 1);
			}

			if (indices.length == 2)
				continue;

			// normal
			nIndices.add(Integer.parseInt(indices[2]) - 1);
			if (nIndices.get(i) < 0)
				nIndices.set(i, object.getNormals().size() + nIndices.get(i) + 1);
		}
		getCurrentGroup().addFace(new ModelFace(vIndices, nIndices.isEmpty() ? null : nIndices.toArray(), tIndices.isEmpty() ? null : tIndices.toArray()));
	}

	private void parseMtllib(String[] words) {
		new MtlParser(object, path, words[1]).parse();
	}

	private void parseUsemtl(String[] words) {
		String name = getCurrentGroup().getName() + " " + words[1];
		setCurrentGroup(new ModelGroup(name));
		getCurrentGroup().setMaterial(object.getMaterials().get(words[1]));
	}

	private void parseGroup(String[] words) {
		setCurrentGroup(new ModelGroup(words.length == 1 ? "default" : words[1]));
	}

	private ModelGroup getCurrentGroup() {
		if (currentGroup == null)
			setCurrentGroup(new ModelGroup("default"));
		return currentGroup;
	}

	private void setCurrentGroup(ModelGroup group) {
		object.getGroups().add(group);
		currentGroup = group;
	}

}
