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
package io.mapsquare.osmcontributor.ui.adapters.item;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class TagItem implements Parcelable {
    private String key;
    private String value;
    private boolean mandatory;
    private List<String> autocompleteValues = new ArrayList<>();
    private TagType type;

    /**
     * Use the best UI widget based on the tag name and possible values.
     */
    public enum TagType {
        OPENING_HOURS,      // Use when tag value is opening_hours
        MULTI_CHOICE,       // Use when a tag can contain multiple values
        BOOLEAN_CHOICE,     // Use when tag value can be yes, no or undefined
        LIST,               // Use when tag value must be choose in a list of element
        TEXT_IMPOSED,       // Use when tag value can't be modified
        TEXT                // Use by default
    }

    public TagItem(String key, String value, boolean mandatory, List<String> autocompleteValues, TagType separator) {
        this.key = key;
        this.value = value;
        this.autocompleteValues = autocompleteValues;
        this.mandatory = mandatory;
        this.type = separator;
    }

    public TagItem(Parcel in) {
        this.key = in.readString();
        this.value = in.readString();
        this.mandatory = in.readByte() != 0;
        try {
            this.type = TagType.valueOf(in.readString());
        } catch (IllegalArgumentException x) {
            this.type = null;
        }

        final int size = in.readInt();

        for (int i = 0; i < size; i++) {
            final String value = in.readString();
            this.autocompleteValues.add(value);
        }
    }

    public List<String> getAutocompleteValues() {
        return autocompleteValues;
    }

    public void setAutocompleteValues(List<String> autocompleteValues) {
        this.autocompleteValues = autocompleteValues;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public TagType getTagType() {
        return type;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(value);
        dest.writeByte((byte) (mandatory ? 1 : 0));
        dest.writeString((type == null) ? "" : type.toString());

        if (autocompleteValues != null) {
            dest.writeInt(autocompleteValues.size());
            for (String autocomplete : autocompleteValues) {
                dest.writeString(autocomplete);
            }
        } else {
            dest.writeInt(0);
        }
    }


    public static final Parcelable.Creator<TagItem> CREATOR = new Parcelable.Creator<TagItem>() {
        @Override
        public TagItem createFromParcel(Parcel source) {
            return new TagItem(source);
        }

        @Override
        public TagItem[] newArray(int size) {
            return new TagItem[size];
        }
    };

}
