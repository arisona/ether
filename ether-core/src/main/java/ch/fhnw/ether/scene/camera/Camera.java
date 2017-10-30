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

package ch.fhnw.ether.scene.camera;

import ch.fhnw.util.UpdateRequest;
import ch.fhnw.util.math.Vec3;
import ch.fhnw.util.math.geometry.BoundingBox;

public final class Camera implements ICamera {
	private static final Vec3 DEFAULT_POSITION = new Vec3(0, -5, 0);
	private static final Vec3 DEFAULT_TARGET = Vec3.ZERO;
	private static final Vec3 DEFAULT_UP = Vec3.Z;
	private static final float DEFAULT_FOV = 45;
	private static final float DEFAULT_NEAR = 0.01f;
	private static final float DEFAULT_FAR = 100000f;

	private final UpdateRequest update = new UpdateRequest();

	private Vec3  position;
	private Vec3  target;
	private Vec3  up;
	private Vec3  orbit = Vec3.Z;

	private float fov;
	private float near;
	private float far;

	private String name = "camera";

	public Camera() {
		this(DEFAULT_POSITION, DEFAULT_TARGET, DEFAULT_UP, DEFAULT_FOV, DEFAULT_NEAR, DEFAULT_FAR);
	}

	public Camera(Vec3 position, Vec3 target) {
		this(position, target, DEFAULT_FOV, DEFAULT_NEAR, DEFAULT_FAR);
	}

	public Camera(Vec3 position, Vec3 target, float fov, float near, float far) {
		this(position, target, Vec3.Z, fov, near, far);
		up = getCameraYAxis();
		if (up.isZero())
			up = Vec3.Y;
	}

	public Camera(Vec3 position, Vec3 target, Vec3 up, float fov, float near, float far) {
		this.position = position;
		this.target = target;
		this.up = up;
		this.fov = fov;
		this.near = near;
		this.far = far;
	}

	@Override
	public BoundingBox getBounds() {
		BoundingBox b = new BoundingBox();
		b.add(getPosition());
		return b;
	}

	@Override
	public Vec3 getPosition() {
		return position;
	}

	@Override
	public void setPosition(Vec3 position) {
		this.position = position;
		updateRequest();
	}

	@Override
	public Vec3 getTarget() {
		return target;
	}

	@Override
	public void setTarget(Vec3 target) {
		this.target = target;
		updateRequest();
	}

	@Override
	public Vec3 getUp() {
		return up;
	}

	@Override
	public void setUp(Vec3 up) {
		this.up = up;
		updateRequest();
	}

	@Override
	public float getFov() {
		return fov;
	}

	@Override
	public void setFov(float fov) {
		this.fov = fov;
		updateRequest();
	}

	@Override
	public float getNear() {
		return near;
	}

	@Override
	public void setNear(float near) {
		this.near = near;
		updateRequest();
	}

	@Override
	public float getFar() {
		return far;
	}

	@Override
	public void setFar(float far) {
		this.far = far;
		updateRequest();
	}

	@Override
	public Vec3 getCameraXAxis() {
		return up.cross(getCameraZAxis()).normalize();
	}

	@Override
	public Vec3 getCameraYAxis() {
		return getCameraZAxis().cross(getCameraXAxis()).normalize();
	}

	@Override
	public Vec3 getCameraZAxis() {
		return position.subtract(target).normalize();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
		updateRequest();
	}
	
	@Override
	public UpdateRequest getUpdater() {
		return update;
	}

	// we purposely leave equals and hashcode at default (identity)
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public String toString() {
		return "camera '" + getName() + "' [" + position + target + up + "][" + getCameraXAxis() + getCameraYAxis() + getCameraZAxis() + "]";
	}
	
	private void updateRequest() {
		update.request();
	}

	@Override
	public void setOrbitAxis(Vec3 axis) {
		this.orbit = axis;
		updateRequest();
	}

	@Override
	public Vec3 getCameraOrbitAxis() {
		return orbit;
	}
}
