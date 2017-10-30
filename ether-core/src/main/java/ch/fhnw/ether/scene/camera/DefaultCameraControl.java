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

import ch.fhnw.util.math.Mat4;
import ch.fhnw.util.math.Vec3;

public class DefaultCameraControl {
	private static final boolean KEEP_ROT_X_POSITIVE = true;
	private static final float MIN_DISTANCE = 0.01f;

	private final ICamera camera;

	public DefaultCameraControl(ICamera camera) {
		this.camera = camera;
	}

	public ICamera getCamera() {
		return camera;
	}

	// convenience methods for basic view parameters

	public void setPosition(Vec3 position) {
		camera.setPosition(position);
	}

	public void setTarget(Vec3 target) {
		camera.setTarget(target);
	}

	public void setUp(Vec3 up) {
		camera.setUp(up);
	}

	// track, pan, dolly, roll (in camera coordinate system, updates target)

	public void track(float deltaX, float deltaY) {
		Vec3 d = camera.getCameraXAxis().scale(deltaX).add(camera.getCameraYAxis().scale(deltaY));
		camera.setPosition(camera.getPosition().add(d));
		camera.setTarget(camera.getTarget().add(d));
	}

	public void pan(float deltaX, float deltaY) {
		// TODO: implement
		throw new UnsupportedOperationException();
	}

	public void dolly(float deltaZ) {
		// TODO: implement
		throw new UnsupportedOperationException();
	}

	public void roll(float deltaZ) {
		// TODO: implement
		throw new UnsupportedOperationException();
	}

	/**
	 * Orbit around target with camera orbit axis. Positive value orbits
	 * counter-clock-wise around axis.
	 * 
	 * @param delta
	 *            relative angle in degrees
	 */
	public void addToAzimuth(float delta) {
		Mat4 m = Mat4.multiply(Mat4.translate(camera.getTarget()), Mat4.rotate(delta, camera.getCameraOrbitAxis()),
				Mat4.translate(camera.getTarget().negate()));

		Vec3 p = m.transform(camera.getPosition());
		Vec3 u = m.transform(camera.getPosition().add(camera.getUp())).subtract(p);

		camera.setPosition(p);
		camera.setUp(u);
	}

	/**
	 * Orbit around target on camera-x axis. Positive value orbits clock-wise
	 * around camera-x axis, i.e. moves camera "up"
	 * 
	 * @param delta
	 *            relative angle in degrees
	 */
	public void addToElevation(float delta) {
		Mat4 m = Mat4.multiply(Mat4.translate(camera.getTarget()), Mat4.rotate(-delta, camera.getCameraXAxis()),
				Mat4.translate(camera.getTarget().negate()));

		Vec3 p = m.transform(camera.getPosition());
		Vec3 u = m.transform(camera.getPosition().add(camera.getUp())).subtract(p);

		if (KEEP_ROT_X_POSITIVE && Vec3.Z.dot(u) < 0)
			return;

		camera.setPosition(p);
		camera.setUp(u);
	}

	public float getDistance() {
		Vec3 p = camera.getPosition();
		Vec3 t = camera.getTarget();
		return p.distance(t);
	}

	public void setDistance(float distance) {
		Vec3 t = camera.getTarget();
		Vec3 z = camera.getCameraZAxis().scale(distance);
		camera.setPosition(t.add(z));
	}

	public void addToDistance(float delta) {
		Vec3 p = camera.getPosition();
		Vec3 t = camera.getTarget();
		Vec3 z = camera.getCameraZAxis().scale(delta);
		p = p.add(z);
		if (delta < 0 && (p.distance(t) < MIN_DISTANCE || p.subtract(t).dot(z) > 0))
			setDistance(MIN_DISTANCE);
		else
			camera.setPosition(p);
	}
}
