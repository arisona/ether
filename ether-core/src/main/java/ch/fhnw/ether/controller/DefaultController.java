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

package ch.fhnw.ether.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import ch.fhnw.ether.controller.event.DefaultEventScheduler;
import ch.fhnw.ether.controller.event.IEventScheduler.IAction;
import ch.fhnw.ether.controller.event.IEventScheduler.IAnimationAction;
import ch.fhnw.ether.controller.event.IKeyEvent;
import ch.fhnw.ether.controller.event.IPointerEvent;
import ch.fhnw.ether.controller.tool.ITool;
import ch.fhnw.ether.controller.tool.NavigationTool;
import ch.fhnw.ether.controller.tool.PickTool;
import ch.fhnw.ether.media.IScheduler;
import ch.fhnw.ether.platform.Platform;
import ch.fhnw.ether.render.DefaultRenderManager;
import ch.fhnw.ether.render.IRenderManager;
import ch.fhnw.ether.render.IRenderer;
import ch.fhnw.ether.render.forward.ForwardRenderer;
import ch.fhnw.ether.scene.IScene;
import ch.fhnw.ether.scene.camera.ICamera;
import ch.fhnw.ether.view.IView;

/**
 * Default controller that implements some basic common functionality. Use as
 * base for more complex implementations.
 *
 * @author radar
 */
// FIXME: PickTool doesn't really belong here (any tools at all?)
public class DefaultController implements IController {
	public static final float DEFAULT_FPS = 60f;
	
	private static final boolean DBG = false;

	private final IRenderManager renderManager;
	private final DefaultEventScheduler scheduler;

	private IScene scene;

	private final ArrayList<IView> views = new ArrayList<>();

	private IView currentView;
	private ITool tool;

	public DefaultController() {
		this(DEFAULT_FPS);
	}
	
	public DefaultController(IRenderer renderer) {
		this(renderer, DEFAULT_FPS);
	}
	
	public DefaultController(float fps) {
		this(new ForwardRenderer());
	}

	public DefaultController(IRenderer renderer, float fps) {
		this.renderManager = new DefaultRenderManager(this, renderer);
		this.scheduler = new DefaultEventScheduler(() -> renderManager.update(), fps);
		run(time -> {
			this.tool = createDefaultTool();
		});
		currentView = null;
	}

	protected ITool createDefaultTool() {
		return new NavigationTool(this, new PickTool(this));
	}

	@Override
	public final IScene getScene() {
		return scene;
	}

	@Override
	public final void setScene(IScene scene) {
		renderManager.clear();
		this.scene = scene;
	}

	@Override
	public final List<IView> getViews() {
		return Collections.unmodifiableList(views);
	}

	@Override
	public final IView getCurrentView() {
		return currentView;
	}

	@Override
	public final void enableViews(Collection<IView> views) {
		if (views != null) {
			for (IView view : this.views) {
				view.setEnabled(views.contains(view));
			}
		} else {
			for (IView view : this.views) {
				view.setEnabled(true);
			}
		}
	}
	
	@Override
	public ICamera getCamera(IView view) {
		return renderManager.getCamera(view);
	}
	
	@Override
	public void setCamera(IView view, ICamera camera) {
		renderManager.setCamera(view, camera);
	}

	@Override
	public final ITool getTool() {
		return tool;
	}

	@Override
	public final void setTool(ITool tool) {
		if (tool == null)
			tool = ITool.NULL_TOOL;

		if (this.tool == tool)
			return;

		this.tool.deactivate();
		this.tool = tool;
		this.tool.activate();
		IView view = getCurrentView();
		if (view != null)
			this.tool.refresh(view);
	}

	@Override
	public final IRenderManager getRenderManager() {
		return renderManager;
	}

	@Override
	public void animate(IAnimationAction action) {
		scheduler.animate(action);
	}
	
	@Override
	public void kill(IAnimationAction action) {
		scheduler.kill(action);
	}
	
	@Override
	public void run(IAction action) {
		scheduler.run(action);
	}
	
	@Override
	public void run(double delay, IAction action) {
		scheduler.run(delay, action);
	}

	@Override
	public final void repaint() {
		scheduler.repaint();
	}
	
	@Override
	public boolean isSceneThread() {
		return scheduler.isSchedulerThread();
	}
	
	@Override
	public void ensureSceneThread() {
		if (!isSceneThread())
			throw new IllegalThreadStateException("must be called on scene thread");		
	}

	// view listener

	@Override
	public final void viewCreated(IView view) {
		if (DBG)
			System.out.println("view created " + view);

		views.add(view);
		renderManager.addView(view);
	}

	@Override
	public void viewDisposed(IView view) {
		if (DBG)
			System.out.println("view disposed " + view);

		views.remove(view);
		if (currentView == view) {
			tool.deactivate();
			setCurrentView(null);
		}
		renderManager.removeView(view);
	}

	@Override
	public void viewGainedFocus(IView view) {
		if (DBG)
			System.out.println("view gained focus " + view);

		setCurrentView(view);
		tool.activate();
	}

	@Override
	public void viewLostFocus(IView view) {
		if (DBG)
			System.out.println("view lost focus " + view);

		if (view == currentView) {
			tool.deactivate();
			setCurrentView(null);
		}
	}
	
	@Override
	public void viewResized(IView view) {
		if (DBG)
			System.out.println("view resized " + view);

		getCamera(view).getUpdater().request();
		tool.refresh(view);
	}

	// called if current camera changed
	@Override
	public void viewChanged(IView view) {
		if (DBG)
			System.out.println("view changed " + view);

		tool.refresh(view);
	}

	// key listener

	@Override
	public void keyPressed(IKeyEvent e) {
		if (DBG)
			System.out.println("key pressed " + e.getView());

		setCurrentView(e.getView());

		// always handle ESC (if not handled by button)
		if (e.getKey() == GLFW.GLFW_KEY_ESCAPE)
			escAction();

		// finally, pass on to tool
		tool.keyPressed(e);
	}
	
	protected void escAction() {
		Platform.get().runOnMainThread(() -> Platform.get().exit());
	}
	
	@Override
	public void keyReleased(IKeyEvent e) {
		if (DBG)
			System.out.println("key released " + e.getView());

		setCurrentView(e.getView());

		// pass on to tool
		tool.keyReleased(e);
	}

	// pointer listener

	@Override
	public void pointerEntered(IPointerEvent e) {
		if (DBG)
			System.out.println("pointer entered " + e.getView());
	}

	@Override
	public void pointerExited(IPointerEvent e) {
		if (DBG)
			System.out.println("pointer exited " + e.getView());
	}

	@Override
	public void pointerPressed(IPointerEvent e) {
		if (DBG)
			System.out.println("pointer pressed " + e.getView());

		setCurrentView(e.getView());

		tool.pointerPressed(e);
	}

	@Override
	public void pointerReleased(IPointerEvent e) {
		if (DBG)
			System.out.println("pointer released " + e.getView());

		tool.pointerReleased(e);
	}

	@Override
	public void pointerClicked(IPointerEvent e) {
		if (DBG)
			System.out.println("pointer clicked " + e.getView());
		
		tool.pointerClicked(e);
	}

	// pointer motion listener

	@Override
	public void pointerMoved(IPointerEvent e) {
		if (DBG)
			System.out.println("pointer moved " + e.getView());

		tool.pointerMoved(e);
	}

	@Override
	public void pointerDragged(IPointerEvent e) {
		if (DBG)
			System.out.println("pointer dragged " + e.getView());

		tool.pointerDragged(e);
	}

	// pointer scrolled listener

	@Override
	public void pointerScrolled(IPointerEvent e) {
		if (DBG)
			System.out.println("pointer scrolled " + e.getView());

		tool.pointerScrolled(e);
	}

	public static void printHelp(String[] help) {
		for (String s : help)
			System.out.println(s);
	}

	// private stuff

	private void setCurrentView(IView view) {
		if (DBG)
			System.out.println("set current view " + view);

		if (currentView != view) {
			currentView = view;
			if (currentView != null)
				getTool().refresh(currentView);
		}
	}

	@Override
	public IScheduler getScheduler() {
		return scheduler;
	}
}
