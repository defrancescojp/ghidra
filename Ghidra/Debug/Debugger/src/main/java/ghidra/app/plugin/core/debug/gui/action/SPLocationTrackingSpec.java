/* ###
 * IP: GHIDRA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ghidra.app.plugin.core.debug.gui.action;

import javax.swing.Icon;

import ghidra.app.plugin.core.debug.gui.DebuggerResources.TrackLocationAction;
import ghidra.debug.api.action.*;
import ghidra.debug.api.tracemgr.DebuggerCoordinates;
import ghidra.framework.plugintool.ServiceProvider;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSpace;
import ghidra.program.model.lang.Register;
import ghidra.program.util.ProgramLocation;
import ghidra.trace.model.TraceAddressSnapRange;
import ghidra.trace.model.guest.TracePlatform;
import ghidra.trace.model.stack.TraceStack;

public enum SPLocationTrackingSpec implements RegisterLocationTrackingSpec {
	INSTANCE;

	private static final SPByStackLocationTrackingSpec BY_STACK =
		SPByStackLocationTrackingSpec.INSTANCE;

	public static final String CONFIG_NAME = "TRACK_SP";

	@Override
	public String getConfigName() {
		return CONFIG_NAME;
	}

	@Override
	public String getMenuName() {
		return TrackLocationAction.NAME_SP;
	}

	@Override
	public Icon getMenuIcon() {
		return TrackLocationAction.ICON_SP;
	}

	@Override
	public String getLocationLabel() {
		return "sp";
	}

	@Override
	public Register computeRegister(DebuggerCoordinates coordinates) {
		TracePlatform platform = coordinates.getPlatform();
		if (platform == null) {
			return null;
		}
		return platform.getCompilerSpec().getStackPointer();
	}

	@Override
	public AddressSpace computeDefaultAddressSpace(DebuggerCoordinates coordinates) {
		return coordinates.getPlatform().getCompilerSpec().getStackBaseSpace();
	}

	@Override
	public Address computeTraceAddress(ServiceProvider provider, DebuggerCoordinates coordinates) {
		if (coordinates.getTime().isSnapOnly()) {
			Address sp = BY_STACK.computeTraceAddress(provider, coordinates);
			if (sp != null) {
				return sp;
			}
		}
		return RegisterLocationTrackingSpec.super.computeTraceAddress(provider, coordinates);
	}

	@Override
	public GoToInput getDefaultGoToInput(ServiceProvider provider, DebuggerCoordinates coordinates,
			ProgramLocation location) {
		Register register = computeRegister(coordinates);
		return GoToInput.offsetOnly(register.getName());
	}

	// Note it does no good to override affectByRegChange. It must do what we'd avoid anyway.
	@Override
	public boolean affectedByStackChange(TraceStack stack, DebuggerCoordinates coordinates) {
		return BY_STACK.affectedByStackChange(stack, coordinates);
	}

	@Override
	public boolean affectedByBytesChange(AddressSpace space, TraceAddressSnapRange range,
			DebuggerCoordinates coordinates) {
		return RegisterLocationTrackingSpec.super.affectedByBytesChange(space, range, coordinates);
	}

	@Override
	public boolean shouldDisassemble() {
		return false;
	}
}
