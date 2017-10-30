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

import ch.fhnw.ether.scene.I3DObject;
import ch.fhnw.util.math.Vec3;

public interface ICamera extends I3DObject {
	// view parameters

	/**
	 * Get camera position.
	 */
	@Override
	Vec3 getPosition();

	/**
	 * Set camera position.
	 */
	@Override
	void setPosition(Vec3 position);

	/**
	 * Get camera target.
	 */
	Vec3 getTarget();

	/**
	 * Set camera target.
	 */
	void setTarget(Vec3 target);

	/**
	 * Get camera up vector.
	 */
	Vec3 getUp();

	/**
	 * Set camera up vector.
	 */
	void setUp(Vec3 up);

	/**
	 * Set camera up orbit axis in world coorsinate system.
	 */
	void setOrbitAxis(Vec3 axis);

	// projection parameters

	/**
	 * Get camera field of view (perspective, fov > 0) or frustum width
	 * (orthographic, fov < 0).
	 */
	float getFov();

	/**
	 * Set field of view of perspective camera (positive fov), or frustum width
	 * of orthographics camera (negative fov).
	 * 
	 * @param fov
	 *            If fov > 0, then a perspective projection is applied with
	 *            selected fov (angle in degrees). If fov < 0, then an
	 *            orthographic projection is applied with -fov being the frustum
	 *            width.
	 */
	void setFov(float fov);

	/**
	 * Get camera near plane.
	 */
	float getNear();

	/**
	 * Set camera near plane.
	 */
	void setNear(float near);

	/**
	 * Get camera far plane.
	 */
	float getFar();

	/**
	 * Set camera far plane.
	 */
	void setFar(float far);

	// camera coordinate system

	/**
	 * Get camera coordinate system x-axis.
	 */
	Vec3 getCameraXAxis();

	/**
	 * Get camera coordinate system y-axis.
	 */
	Vec3 getCameraYAxis();

	/**
	 * Get camera coordinate system z-axis.
	 */
	Vec3 getCameraZAxis();

	/**
	 * Get camera orbit axis in world coordinate system.
	 */
	Vec3 getCameraOrbitAxis();
}
