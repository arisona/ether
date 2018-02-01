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

package ch.fhnw.ether.controller.tool;

import java.util.ArrayList;
import java.util.List;

import ch.fhnw.ether.controller.IController;
import ch.fhnw.ether.controller.event.IKeyEvent;
import ch.fhnw.ether.controller.event.IPointerEvent;
import ch.fhnw.ether.platform.Platform;
import ch.fhnw.ether.platform.Platform.OS;
import ch.fhnw.ether.scene.camera.DefaultCameraControl;
import ch.fhnw.ether.scene.mesh.DefaultMesh;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.IMesh.Primitive;
import ch.fhnw.ether.scene.mesh.IMesh.Queue;
import ch.fhnw.ether.scene.mesh.MeshUtilities;
import ch.fhnw.ether.scene.mesh.geometry.DefaultGeometry;
import ch.fhnw.ether.scene.mesh.material.LineMaterial;
import ch.fhnw.ether.view.IView;
import ch.fhnw.ether.view.IView.ViewFlag;
import ch.fhnw.util.color.RGBA;
import ch.fhnw.util.math.Vec3;

// TODO: grid doesn't really belong here
public class NavigationTool extends AbstractTool {
	public static final RGBA GRID_COLOR = RGBA.GRAY;

	private int button;
	private float mouseX;
	private float mouseY;
	
	private ITool fallbackTool = ITool.NULL_TOOL;

	private IMesh grid;
	private IMesh axes;

	public NavigationTool(IController controller) {
		super(controller);
		grid = makeGrid();
		axes = makeAxes();
	}
	
	public NavigationTool(IController controller, ITool fallbackTool) {
		this(controller);
		this.fallbackTool = fallbackTool;
	}
	
	public ITool getFallbackTool() {
		return fallbackTool;
	}
	
	public void setFallbackTool(ITool fallbackTool) {
		this.fallbackTool = fallbackTool;
	}

	@Override
	public void activate() {
		IView view = getController().getCurrentView();
		if (view != null && view.getConfig().has(ViewFlag.GRID)) {
			getController().getRenderManager().addMesh(grid);
			getController().getRenderManager().addMesh(axes);
		}
		fallbackTool.activate();
	}

	@Override
	public void deactivate() {
		IView view = getController().getCurrentView();
		if (view != null && getController().getCurrentView().getConfig().has(ViewFlag.GRID)) {
			getController().getRenderManager().removeMesh(grid);
			getController().getRenderManager().removeMesh(axes);
		}
		fallbackTool.deactivate();
	}
	
	@Override
	public void refresh(IView view) {
		fallbackTool.refresh(view);
	}
	
	@Override
	public void keyPressed(IKeyEvent e) {
		fallbackTool.keyPressed(e);
	}
	
	@Override
	public void keyReleased(IKeyEvent e) {
		fallbackTool.keyReleased(e);
	}

	@Override
	public void pointerPressed(IPointerEvent e) {
		if (e.isModifierDown()) {
			mouseX = e.getX();
			mouseY = e.getY();
			button = e.getButton();
		} else {
			fallbackTool.pointerPressed(e);
		}
	}
	
	@Override
	public void pointerReleased(IPointerEvent e) {
		if (e.isModifierDown()) {
			// nop
		} else {
			fallbackTool.pointerReleased(e);
		}
	}
	
	@Override
	public void pointerClicked(IPointerEvent e) {
		if (e.isModifierDown()) {
			// nop
		} else {
			fallbackTool.pointerClicked(e);
		}
	}

	@Override
	public void pointerMoved(IPointerEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
		button = e.getButton();
		fallbackTool.pointerMoved(e);
	}

	@Override
	public void pointerDragged(IPointerEvent e) {
		if (e.isModifierDown()) {
			DefaultCameraControl control = new DefaultCameraControl(getCamera(e.getView()));
			float dx = mouseX - e.getX();
			float dy = mouseY - e.getY();
			float moveFactor = 0.001f * control.getDistance();
			float turnFactor = 0.2f;
			if (button == IPointerEvent.BUTTON_1) {
				control.addToAzimuth(turnFactor * dx);
				control.addToElevation(turnFactor * dy);
			} else if (button == IPointerEvent.BUTTON_2 || button == IPointerEvent.BUTTON_3) {
				control.track(moveFactor * dx, moveFactor * dy);
			}
			mouseX = e.getX();
			mouseY = e.getY();
			getController().viewChanged(e.getView());
		} else {
			fallbackTool.pointerDragged(e);
		}
	}

	// FIXME: find a solution for OS-dependent stuff like this
	@Override
	public void pointerScrolled(IPointerEvent e) {
		DefaultCameraControl control = new DefaultCameraControl(getCamera(e.getView()));
		float zoomFactor = (Platform.getOS() == OS.WINDOWS ? 0.1f : 0.01f) * control.getDistance();
		control.addToAzimuth((Platform.getOS() == OS.WINDOWS ? -5f : -1f) * e.getScrollX());
		if (e.isControlDown()) {
			control.dolly(e.getScrollY() * zoomFactor);
		} else {
			control.addToDistance(e.getScrollY() * zoomFactor);
		}
		getController().viewChanged(e.getView());
	}

	protected IMesh makeGrid() {
		List<Vec3> lines = new ArrayList<>();

		int gridNumLines = 12;
		float gridSpacing = 0.1f;

		// add axis lines
		float e = 0.5f * gridSpacing * (gridNumLines + 1);
		MeshUtilities.addLine(lines, -e, 0, e, 0);
		MeshUtilities.addLine(lines, 0, -e, 0, e);

		// add grid lines
		int n = gridNumLines / 2;
		for (int i = 1; i <= n; ++i) {
			MeshUtilities.addLine(lines, i * gridSpacing, -e, i * gridSpacing, e);
			MeshUtilities.addLine(lines, -i * gridSpacing, -e, -i * gridSpacing, e);
			MeshUtilities.addLine(lines, -e, i * gridSpacing, e, i * gridSpacing);
			MeshUtilities.addLine(lines, -e, -i * gridSpacing, e, -i * gridSpacing);
		}

		return new DefaultMesh(Primitive.LINES, new LineMaterial(RGBA.GRAY), DefaultGeometry.createV(Vec3.toArray(lines)), Queue.TRANSPARENCY);
	}
	
	protected IMesh makeAxes() {
		float[] lines = {
			0, 0, 0, 1, 0, 0, 
			0, 0, 0, 0, 1, 0,
			0, 0, 0, 0, 0, 1 
		};
		float[] colors = {
			1, 0, 0, 1, 1, 0, 0, 1,
			0, 1, 0, 1, 0, 1, 0, 1,
			0, 0, 1, 1, 0, 0, 1, 1
		};

		return new DefaultMesh(Primitive.LINES, new LineMaterial(RGBA.WHITE, true), DefaultGeometry.createVC(lines, colors), Queue.TRANSPARENCY);
	}
}
