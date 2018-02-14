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

package ch.fhnw.ether.examples.threed;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ch.fhnw.ether.controller.IController;
import ch.fhnw.ether.formats.IModelReader.Options;
import ch.fhnw.ether.formats.obj.ObjReader;
import ch.fhnw.ether.formats.obj.ObjWriter;
import ch.fhnw.ether.platform.Platform;
import ch.fhnw.ether.scene.DefaultScene;
import ch.fhnw.ether.scene.IScene;
import ch.fhnw.ether.scene.light.DirectionalLight;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.MeshUtilities;
import ch.fhnw.ether.view.DefaultView;
import ch.fhnw.ether.view.IView;
import ch.fhnw.util.color.RGB;
import ch.fhnw.util.math.Vec3;

public class ObjWriterExample {
	static File out;
	
	public static void main(String[] args) {
		out = new File(args[0]);
		new ObjWriterExample();
	}

	public ObjWriterExample() {
		Platform.get().init();
		IController controller = new ObjLoaderController();
		controller.run(time -> {
			new DefaultView(controller, 100, 100, 512, 512, IView.INTERACTIVE_VIEW, "Obj View");
	
			IScene scene = new DefaultScene(controller);
			controller.setScene(scene);
			
			scene.add3DObject(new DirectionalLight(new Vec3(1, 1, 1), RGB.BLACK, RGB.WHITE));
	
			try {
				final URL obj = getClass().getResource("/models/f16.obj");
				final List<IMesh> meshes = new ArrayList<>();
				new ObjReader(obj, Options.CONVERT_TO_Z_UP).getMeshes().forEach(mesh -> meshes.add(mesh));
				System.out.println("number of meshes before merging: " + meshes.size());
				final List<IMesh> merged = MeshUtilities.mergeMeshes(meshes);
				System.out.println("number of meshes after merging: " + merged.size());
				scene.add3DObjects(merged);
				System.out.println("Writing meshes to '"+out+"'...");
				ObjWriter ow = new ObjWriter(out);
				for(IMesh mesh : merged)
					ow.addMesh(mesh);
				ow.write();
				System.out.println("done.");
				System.exit(0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		Platform.get().run();
	}
}
