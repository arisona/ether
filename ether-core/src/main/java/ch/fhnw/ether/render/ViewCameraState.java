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

package ch.fhnw.ether.render;

import ch.fhnw.ether.scene.camera.ICamera;
import ch.fhnw.ether.scene.camera.IViewCameraState;
import ch.fhnw.ether.view.IView;
import ch.fhnw.util.Viewport;
import ch.fhnw.util.math.Mat3;
import ch.fhnw.util.math.Mat4;
import ch.fhnw.util.math.Vec3;

final class ViewCameraState implements IViewCameraState {
	private final Vec3 position;
	private final Vec3 target;
	private final Vec3 up;

	private final float fov;
	private final float near;
	private final float far;

	
	private final Viewport viewport;
	
	private final boolean locked;

	private volatile Mat4 viewMatrix;
	private volatile Mat4 projMatrix;
	private volatile Mat4 viewProjMatrix;
	private volatile Mat4 viewProjInvMatrix;
	private volatile Mat3 normalMatrix;

	public ViewCameraState(IView view, ICamera camera) {
		this.viewport = view.getViewport();
		this.locked = false;
		this.position = camera.getPosition();
		this.target = camera.getTarget();
		this.up = camera.getUp();
		this.fov = camera.getFov();
		this.near = camera.getNear();
		this.far = camera.getFar();
		if(camera instanceof IProjectionMatrixProvider) {
			this.projMatrix = ((IProjectionMatrixProvider)camera).getProjectioMatrix();
		}
	}

	public ViewCameraState(IView view, Mat4 viewMatrix, Mat4 projMatrix) {
		this.viewport = view.getViewport();
		this.locked = true;
		position = target = up = null;
		fov = near = far = 0;
		this.viewMatrix = viewMatrix;
		this.projMatrix = projMatrix;
	}
	
	@Override
	public Viewport getViewport() {
		return viewport;
	}
	
	@Override
	public boolean isLocked() {
		return locked;
	}

	@Override
	public Mat4 getViewMatrix() {
		if (viewMatrix == null) {
			viewMatrix = Mat4.lookAt(position, target, up);
		}
		return viewMatrix;
	}

	@Override
	public Mat4 getProjMatrix() {
		if (projMatrix == null) {
			float a = viewport.getAspect();
			if (fov > 0) {
				projMatrix = Mat4.perspective(fov, a, near, far);
			} else {
				float f = -fov / 2;
				projMatrix = Mat4.ortho(-f, f, -f / a, f / a, near, far);
			}
		}
		return projMatrix;
	}

	@Override
	public Mat4 getViewProjMatrix() {
		if (viewProjMatrix == null) {
			viewProjMatrix = Mat4.multiply(getProjMatrix(), getViewMatrix());
		}
		return viewProjMatrix;
	}

	@Override
	public Mat4 getViewProjInvMatrix() {
		if (viewProjInvMatrix == null) {
			viewProjInvMatrix = getViewProjMatrix().inverse();
		}
		return viewProjInvMatrix;
	}

	@Override
	public Mat3 getNormalMatrix() {
		if (normalMatrix == null) {
			normalMatrix = new Mat3(getViewMatrix()).inverse().transpose();
		}
		return normalMatrix;
	}
}
