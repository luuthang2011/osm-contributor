/**
 * Copyright (C) 2016 eBusiness Information
 *
 * This file is part of OSM Contributor.
 *
 * OSM Contributor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OSM Contributor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OSM Contributor.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.mapsquare.osmcontributor.rest.events;

import io.mapsquare.osmcontributor.rest.dtos.dma.H2GeoPresetDto;

public class PresetDownloadedEvent {

    private final H2GeoPresetDto preset; // TODO use real model, not DTO

    public PresetDownloadedEvent(H2GeoPresetDto preset) {
        this.preset = preset;
    }

    public H2GeoPresetDto getPreset() {
        return preset;
    }
}