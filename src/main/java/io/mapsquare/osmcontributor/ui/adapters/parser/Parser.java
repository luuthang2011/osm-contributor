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
package io.mapsquare.osmcontributor.ui.adapters.parser;

import java.util.List;

import io.mapsquare.osmcontributor.ui.adapters.item.TagItem;

public interface Parser {

    int PRIORITY_LOW = 100;

    int PRIORITY_NOT_IMPORTANT = 75;

    int PRIORITY_NORMAL = 50;

    int PRIORITY_IMPORTANT = 25;

    int PRIORITY_HIGH = 0;

    TagItem.Type getType();

    boolean isCandidate(String key, List<String> values);

    boolean support(String value);

    int getPriority();
}