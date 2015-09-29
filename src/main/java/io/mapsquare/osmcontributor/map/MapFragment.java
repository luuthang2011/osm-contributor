/**
 * Copyright (C) 2015 eBusiness Information
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
package io.mapsquare.osmcontributor.map;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.events.MapListener;
import com.mapbox.mapboxsdk.events.RotateEvent;
import com.mapbox.mapboxsdk.events.ScrollEvent;
import com.mapbox.mapboxsdk.events.ZoomEvent;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Icon;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.Overlay;
import com.mapbox.mapboxsdk.tileprovider.tilesource.WebSourceTileLayer;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.MapViewListener;
import com.mapbox.mapboxsdk.views.util.TilesLoadedListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import io.mapsquare.osmcontributor.OsmTemplateApplication;
import io.mapsquare.osmcontributor.R;
import io.mapsquare.osmcontributor.core.ConfigManager;
import io.mapsquare.osmcontributor.core.events.NodeRefAroundLoadedEvent;
import io.mapsquare.osmcontributor.core.events.PleaseDeletePoiEvent;
import io.mapsquare.osmcontributor.core.events.PleaseLoadNodeRefAround;
import io.mapsquare.osmcontributor.core.model.Note;
import io.mapsquare.osmcontributor.core.model.Poi;
import io.mapsquare.osmcontributor.core.model.PoiNodeRef;
import io.mapsquare.osmcontributor.core.model.PoiType;
import io.mapsquare.osmcontributor.edition.EditPoiActivity;
import io.mapsquare.osmcontributor.edition.events.PleaseApplyNodeRefPositionChange;
import io.mapsquare.osmcontributor.edition.events.PleaseApplyPoiPositionChange;
import io.mapsquare.osmcontributor.map.events.AddressFoundEvent;
import io.mapsquare.osmcontributor.map.events.EditionVectorialTilesLoadedEvent;
import io.mapsquare.osmcontributor.map.events.NewNoteCreatedEvent;
import io.mapsquare.osmcontributor.map.events.NewPoiTypeSelected;
import io.mapsquare.osmcontributor.map.events.OnBackPressedMapEvent;
import io.mapsquare.osmcontributor.map.events.PleaseApplyNoteFilterEvent;
import io.mapsquare.osmcontributor.map.events.PleaseApplyPoiFilter;
import io.mapsquare.osmcontributor.map.events.PleaseChangePoiPosition;
import io.mapsquare.osmcontributor.map.events.PleaseChangeToolbarColor;
import io.mapsquare.osmcontributor.map.events.PleaseChangeValuesDetailNoteFragmentEvent;
import io.mapsquare.osmcontributor.map.events.PleaseChangeValuesDetailPoiFragmentEvent;
import io.mapsquare.osmcontributor.map.events.PleaseDeletePoiFromMapEvent;
import io.mapsquare.osmcontributor.map.events.PleaseDisplayTutorialEvent;
import io.mapsquare.osmcontributor.map.events.PleaseInitializeNoteDrawerEvent;
import io.mapsquare.osmcontributor.map.events.PleaseLoadEditVectorialTileEvent;
import io.mapsquare.osmcontributor.map.events.PleaseOpenEditionEvent;
import io.mapsquare.osmcontributor.map.events.PleaseSelectNodeRefByID;
import io.mapsquare.osmcontributor.map.events.PleaseSwitchModeEvent;
import io.mapsquare.osmcontributor.map.events.PleaseToggleDrawer;
import io.mapsquare.osmcontributor.map.events.PleaseToggleDrawerLock;
import io.mapsquare.osmcontributor.map.events.VectorialTilesLoadedEvent;
import io.mapsquare.osmcontributor.map.vectorial.Geocoder;
import io.mapsquare.osmcontributor.map.vectorial.LevelBar;
import io.mapsquare.osmcontributor.map.vectorial.VectorialObject;
import io.mapsquare.osmcontributor.map.vectorial.VectorialOverlay;
import io.mapsquare.osmcontributor.note.NoteCommentDialogFragment;
import io.mapsquare.osmcontributor.sync.events.SyncDownloadWayEvent;
import io.mapsquare.osmcontributor.sync.events.SyncFinishUploadNote;
import io.mapsquare.osmcontributor.sync.events.SyncFinishUploadPoiEvent;
import io.mapsquare.osmcontributor.sync.events.error.SyncConflictingNodeErrorEvent;
import io.mapsquare.osmcontributor.sync.events.error.SyncDownloadRetrofitErrorEvent;
import io.mapsquare.osmcontributor.sync.events.error.SyncNewNodeErrorEvent;
import io.mapsquare.osmcontributor.sync.events.error.SyncUnauthorizedEvent;
import io.mapsquare.osmcontributor.sync.events.error.SyncUploadNoteRetrofitErrorEvent;
import io.mapsquare.osmcontributor.sync.events.error.SyncUploadRetrofitErrorEvent;
import io.mapsquare.osmcontributor.sync.events.error.TooManyRequestsEvent;
import io.mapsquare.osmcontributor.utils.FlavorUtils;
import io.mapsquare.osmcontributor.utils.ViewAnimation;
import timber.log.Timber;


public class MapFragment extends Fragment {

    public static final String MAP_FRAGMENT_TAG = "MAP_FRAGMENT_TAG";
    private static final String LOCATION = "location";
    private static final String ZOOM_LEVEL = "zoom level";
    private static final String LEVEL = "level";
    private static final String MARKER_TYPE = "MARKER_TYPE";
    public static final String CREATION_MODE = "CREATION_MODE";
    public static final String SELECTED_MARKER_ID = "SELECTED_MARKER_ID";
    public static final String HIDDEN_POI_TYPE = "HIDDEN_POI_TYPE";
    private static final String DISPLAY_OPEN_NOTES = "DISPLAY_OPEN_NOTES";
    private static final String DISPLAY_CLOSED_NOTES = "DISPLAY_CLOSED_NOTES";

    private LocationMarker markerSelected = null;

    // when resuming app we use this id to re-select the good marker
    private Long markerSelectedId = -1L;
    private MapMode mapMode = MapMode.DEFAULT;

    private boolean isMenuLoaded = false;
    private boolean pleaseSwitchToPoiSelected = false;

    private Map<Long, LocationMarker> markersPoi;
    private Map<Long, LocationMarker> markersNotes;
    private Map<Long, LocationMarker> markersNodeRef;

    private int maxPoiType;
    private PoiType poiTypeSelected;
    private ButteryProgressBar progressBar;
    MapFragmentPresenter presenter;

    @Inject
    BitmapHandler bitmapHandler;

    @InjectView(R.id.mapview)
    MapView mapView;

    @Inject
    EventBus eventBus;

    @InjectView(R.id.poi_detail_wrapper)
    RelativeLayout poiDetailWrapper;

    @InjectView(R.id.progressbar)
    RelativeLayout progressbarWrapper;

    @InjectView(R.id.note_detail_wrapper)
    RelativeLayout noteDetailWrapper;

    @Inject
    SharedPreferences sharedPreferences;

    @Inject
    ConfigManager configManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tracker = ((OsmTemplateApplication) this.getActivity().getApplication()).getTracker(OsmTemplateApplication.TrackerName.APP_TRACKER);
        tracker.setScreenName("MapView");
        tracker.send(new HitBuilders.ScreenViewBuilder().build());

        measureMaxPoiType();

        presenter = new MapFragmentPresenter(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        markersPoi = new HashMap<>();
        markersNotes = new HashMap<>();
        markersNodeRef = new HashMap<>();

        ((OsmTemplateApplication) getActivity().getApplication()).getOsmTemplateComponent().inject(this);
        ButterKnife.inject(this, rootView);
        setHasOptionsMenu(true);


        zoomVectorial = configManager.getZoomVectorial();

        if (savedInstanceState != null) {
            currentLevel = savedInstanceState.getDouble(LEVEL);
            selectedMarkerType = LocationMarker.MarkerType.values()[savedInstanceState.getInt(MARKER_TYPE)];
        }

        instantiateProgressBar();

        instantiateMapView(savedInstanceState);

        instantiateLevelBar();


        Timber.d("bounding box : %s", getViewBoundingBox());
        Timber.d("bounding box internal : %s", mapView.getBoundingBoxInternal());

        return rootView;
    }

    private void instantiateProgressBar() {
        progressBar = new ButteryProgressBar(getActivity());
        progressBar.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 24));
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        p.addRule(RelativeLayout.BELOW, R.id.poi_type_spinner_wrapper);
        progressBar.setVisibility(View.GONE);
        progressBar.setLayoutParams(p);
        progressbarWrapper.addView(progressBar);
    }

    private void instantiateLevelBar() {
        Drawable d = getResources().getDrawable(R.drawable.level_thumb);
        levelBar.setThumb(d);
        levelBar.setDrawableHeight(d.getIntrinsicHeight());
        levelBar.setLevel(currentLevel);
        levelBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Timber.v("onProgressChanged");
                if (vectorialOverlay != null) {
                    LevelBar lvl = (LevelBar) seekBar;
                    currentLevel = lvl.getLevel();
                    vectorialOverlay.setLevel(currentLevel);
                    invalidateMap();
                    applyPoiFilter();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void instantiateMapView(Bundle savedInstanceState) {
        // Create a TileSource from OpenStreetMap
        WebSourceTileLayer osmTileLayer = new WebSourceTileLayer("openstreetmap", configManager.getMapUrl());
        osmTileLayer.setName("OpenStreetMap")
                .setAttribution("© OpenStreetMap Contributors")
                .setMinimumZoomLevel(5)
                .setMaximumZoomLevel(22);

        // Set the OpenStreetMap tile source as tile source
        mapView.setTileSource(osmTileLayer);

        // Set the map bounds
        if (configManager.hasBounds()) {
            mapView.setScrollableAreaLimit(configManager.getBoundingBox());
        }

        // disable rotation of the map
        mapView.setMapRotationEnabled(false);

        // Enable disk cache
        mapView.setDiskCacheEnabled(true);

        mapView.setUserLocationEnabled(true);
        // Set the map center and zoom to the saved values or use the default values
        if (savedInstanceState == null) {
            mapView.setCenter((FlavorUtils.isStore() && mapView.getUserLocation() != null) ? mapView.getUserLocation() : configManager.getDefaultCenter());
            mapView.setZoom(configManager.getDefaultZoom());
        } else {
            mapView.setCenter((LatLng) savedInstanceState.getParcelable(LOCATION));
            mapView.setZoom(savedInstanceState.getFloat(ZOOM_LEVEL));
        }

        mapView.setOnTilesLoadedListener(new TilesLoadedListener() {
            @Override
            public boolean onTilesLoaded() {
                return false;
            }

            @Override
            public boolean onTilesLoadStarted() {
                return false;
            }
        });


        mapView.addListener(new MapListener() {
            int initialX;
            int initialY;
            int deltaX;
            int deltaY;

            @Override
            public void onScroll(ScrollEvent scrollEvent) {
                deltaX = initialX - scrollEvent.getX();
                deltaY = initialY - scrollEvent.getY();

                // 20 px delta before it's worth checking
                int minPixelsDeltaBeforeCheck = 100;

                if (Math.abs(deltaX) > minPixelsDeltaBeforeCheck || Math.abs(deltaY) > minPixelsDeltaBeforeCheck) {
                    initialX = scrollEvent.getX();
                    initialY = scrollEvent.getY();
                    presenter.loadPoisIfNeeded();
                    presenter.loadVectorialTilesIfNeeded();

                    if (getZoomLevel() > zoomVectorial) {
                        LatLng center = mapView.getCenter();
                        geocoder.delayedReverseGeocoding(center.getLatitude(), center.getLongitude());
                    }
                }
            }

            @Override
            public void onZoom(ZoomEvent zoomEvent) {
                presenter.loadPoisIfNeeded();
                Timber.v("new zoom : %s", zoomEvent.getZoomLevel());
                presenter.loadVectorialTilesIfNeeded();

                if (zoomEvent.getZoomLevel() < zoomVectorial) {
                    levelBar.setVisibility(View.INVISIBLE);
                    addressView.setVisibility(View.INVISIBLE);
                    if (isVectorial) {
                        isVectorial = false;
                        applyPoiFilter();
                        applyNoteFilter();
                    }
                } else {
                    LatLng center = mapView.getCenter();
                    geocoder.delayedReverseGeocoding(center.getLatitude(), center.getLongitude());
                    if (levelBar.getLevels().length > 1) {
                        levelBar.setVisibility(View.VISIBLE);
                    }
                    if (!isVectorial) {
                        isVectorial = true;
                        applyPoiFilter();
                        applyNoteFilter();
                    }
                }
            }

            @Override
            public void onRotate(RotateEvent rotateEvent) {
                presenter.loadPoisIfNeeded();
                presenter.loadVectorialTilesIfNeeded();
            }

        });

        mapView.setMapViewListener(new MapViewListener() {
            @Override
            public void onShowMarker(MapView mapView, Marker marker) {
            }

            @Override
            public void onHideMarker(MapView mapView, Marker marker) {
            }

            @Override
            public void onTapMarker(MapView mapView, Marker marker) {
                if (marker instanceof LocationMarker) {
                    LocationMarker locationMarker = (LocationMarker) marker;
                    if (mapMode != MapMode.POI_POSITION_EDITION && mapMode != MapMode.CREATION && mapMode != MapMode.WAY_EDITION && !isTuto) {
                        switch (locationMarker.getType()) {
                            case POI:
                                onPoiMarkerClick(locationMarker);
                                break;
                            case NOTE:
                                onNoteMarkerClick(locationMarker);
                                break;
                            default:
                                break;
                        }
                    }
                }

            }

            @Override
            public void onLongPressMarker(MapView mapView, Marker marker) {

            }

            @Override
            public void onTapMap(MapView mapView, ILatLng iLatLng) {
                if (mapMode == MapMode.DETAIL_POI || mapMode == MapMode.DETAIL_NOTE) {
                    // it prevents to reselect the marker
                    markerSelectedId = -1L;
                    switchMode(MapMode.DEFAULT);
                }
                if (mapMode == MapMode.WAY_EDITION) {
                    eventBus.post(new PleaseLoadNodeRefAround(iLatLng.getLatitude(), iLatLng.getLongitude()));
                }
                if (mapMode == MapMode.DEFAULT && floatingMenuAddFewValues.isExpanded()) {
                    floatingMenuAddFewValues.collapse();
                }

            }

            @Override
            public void onLongPressMap(MapView mapView, ILatLng iLatLng) {

            }
        });
    }

    private void measureMaxPoiType() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        float toolbarSize = getResources().getDimension(R.dimen.abc_action_bar_default_height_material) / displayMetrics.density;
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;

        // 80 size of one floating btn in dp
        // 180 size of the menu btn plus location btn
        // -1 because we always have note

        maxPoiType = (int) ((dpHeight - toolbarSize - 160) / 80) - 1;
    }

    void onPoiMarkerClick(LocationMarker marker) {
        unselectIcon();
        markerSelected = (marker);
        Bitmap bitmap = bitmapHandler.getMarkerBitmap(markerSelected.getPoi().getType().getId(), Poi.computeState(true, false, false));
        if (bitmap != null) {
            markerSelected.setIcon(new Icon(new BitmapDrawable(getResources(), bitmap)));
        }
        selectedMarkerType = LocationMarker.MarkerType.POI;
        switchMode(MapMode.DETAIL_POI);
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, OsmAnimatorUpdateListener.STEPS_CENTER_ANIMATION);
        valueAnimator.setDuration(500);
        valueAnimator.addUpdateListener(new OsmAnimatorUpdateListener(mapView.getCenter(), markerSelected.getPoint(), mapView));
        valueAnimator.start();
        markerSelectedId = -1L;
    }


    private void onNodeRefClick(LocationMarker marker) {
        editNodeRefPosition.setVisibility(View.VISIBLE);
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, OsmAnimatorUpdateListener.STEPS_CENTER_ANIMATION);
        valueAnimator.setDuration(500);
        valueAnimator.addUpdateListener(new OsmAnimatorUpdateListener(mapView.getCenter(), marker.getPoint(), mapView));
        valueAnimator.start();
    }

    private void onNoteMarkerClick(LocationMarker marker) {
        unselectIcon();
        markerSelected = (marker);
        selectedMarkerType = LocationMarker.MarkerType.NOTE;

        Bitmap bitmap = bitmapHandler.getNoteBitmap(Note.computeState(markerSelected.getNote(), true, false));
        if (bitmap != null) {
            markerSelected.setIcon(new Icon(new BitmapDrawable(getResources(), bitmap)));
        }

        switchMode(MapMode.DETAIL_NOTE);
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, OsmAnimatorUpdateListener.STEPS_CENTER_ANIMATION);
        valueAnimator.setDuration(500);
        valueAnimator.addUpdateListener(new OsmAnimatorUpdateListener(mapView.getCenter(), markerSelected.getPoint(), mapView));
        valueAnimator.start();
        markerSelectedId = -1L;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            Integer creationModeInt = savedInstanceState.getInt(CREATION_MODE);
            markerSelectedId = savedInstanceState.getLong(SELECTED_MARKER_ID, -1);

            mapMode = MapMode.values()[creationModeInt];
            if (mapMode == MapMode.DETAIL_NOTE || mapMode == MapMode.DETAIL_POI || mapMode == MapMode.POI_POSITION_EDITION) {
                mapMode = MapMode.DEFAULT;
            } else if (mapMode == MapMode.NODE_REF_POSITION_EDITION) {
                mapMode = MapMode.WAY_EDITION;
            }

            long[] hidden = savedInstanceState.getLongArray(HIDDEN_POI_TYPE);
            if (hidden != null) {
                for (long l : hidden) {
                    poiTypeHidden.add(l);
                }
            }

            displayOpenNotes = savedInstanceState.getBoolean(DISPLAY_OPEN_NOTES);
            displayClosedNotes = savedInstanceState.getBoolean(DISPLAY_CLOSED_NOTES);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.register();

        //enable geolocation of user
        mapView.setUserLocationEnabled(true);

        eventBus.register(this);
        presenter.setForceRefreshPoi();
        presenter.setForceRefreshNotes();
        presenter.loadPoisIfNeeded();
        presenter.loadVectorialTilesIfNeeded();
        eventBus.post(new PleaseInitializeNoteDrawerEvent(displayOpenNotes, displayClosedNotes));
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        presenter.unregister();
        mapView.setUserLocationEnabled(false);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clear bitmapHandler even if activity leaks.
        bitmapHandler = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(LOCATION, mapView.getCenter());
        outState.putFloat(ZOOM_LEVEL, getZoomLevel());
        outState.putInt(CREATION_MODE, mapMode.ordinal());
        outState.putDouble(LEVEL, currentLevel);
        outState.putBoolean(DISPLAY_OPEN_NOTES, displayOpenNotes);
        outState.putBoolean(DISPLAY_CLOSED_NOTES, displayClosedNotes);

        int markerType = markerSelected == null ? LocationMarker.MarkerType.NONE.ordinal() : markerSelected.getType().ordinal();
        outState.putInt(MARKER_TYPE, markerType);

        if (markerSelected != null) {
            switch (markerSelected.getType()) {
                case POI:
                    markerSelectedId = markerSelected.getPoi().getId();
                    break;
                case NODE_REF:
                    markerSelectedId = markerSelected.getNodeRef().getId();
                    break;
                case NOTE:
                    markerSelectedId = markerSelected.getNote().getId();
                    break;
            }
        } else {
            markerSelectedId = -1L;
        }
        outState.putLong(SELECTED_MARKER_ID, markerSelectedId);


        long[] hidden = new long[poiTypeHidden.size()];
        int index = 0;

        for (Long value : poiTypeHidden) {
            hidden[index] = value;
            index++;
        }

        outState.putLongArray(HIDDEN_POI_TYPE, hidden);
    }

    /*-----------------------------------------------------------
    * ANALYTICS ATTRIBUTES
    *---------------------------------------------------------*/

    private Tracker tracker;

    private enum Category {
        MapMode("Map Mode"),
        GeoLocation("Geolocation"),
        Edition("Edition POI");

        private final String value;

        Category(String value) {
            this.value = value;
        }

        String getValue() {
            return value;
        }
    }

    /*-----------------------------------------------------------
    * ACTIONBAR
    *---------------------------------------------------------*/

    private MenuItem filter;
    private MenuItem confirm;
    private MenuItem downloadArea;
    private boolean homeActionBtnMode = true;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        displayHomeButton(true); //show the home button
        confirm = menu.findItem(R.id.action_confirm_position);
        downloadArea = menu.findItem(R.id.action_download_area);
        filter = menu.findItem(R.id.action_filter_drawer);
        // the menu has too be created because we modify it depending on mode
        isMenuLoaded = true;
        if (pleaseSwitchToPoiSelected) {
            pleaseSwitchToPoiSelected = false;
            onMarkerClick(markerSelected);
        } else {
            switchMode(mapMode);
        }
    }

    public void onMarkerClick(LocationMarker marker) {
        if (marker.isPoi()) {
            onPoiMarkerClick(marker);
        } else {
            onNoteMarkerClick(marker);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_confirm_position:
                confirmPosition();
                break;

            case R.id.action_download_area:
                onDownloadZoneClick();
                break;

            case android.R.id.home:
                if (homeActionBtnMode) {
                    eventBus.post(new PleaseToggleDrawer());
                } else {
                    eventBus.post(new OnBackPressedMapEvent());
                }
                break;

            default:
                super.onOptionsItemSelected(item);
                break;
        }


        return true;
    }

    private void confirmPosition() {
        LatLng newPoiPosition;

        switch (mapMode) {
            case CREATION:

                PoiType poiType = (PoiType) spinner.getSelectedItem();
                LatLng pos = mapView.getCenter();

                if (poiType != null) {

                    switchMode(MapMode.DEFAULT);

                    Intent intent = new Intent(getActivity(), EditPoiActivity.class);
                    intent.putExtra(EditPoiActivity.CREATION_MODE, true);
                    intent.putExtra(EditPoiActivity.POI_LAT, pos.getLatitude());
                    intent.putExtra(EditPoiActivity.POI_LNG, pos.getLongitude());
                    intent.putExtra(EditPoiActivity.POI_LEVEL, isVectorial ? currentLevel : 0d);
                    intent.putExtra(EditPoiActivity.POI_TYPE, poiType.getId());

                    getActivity().startActivityForResult(intent, EditPoiActivity.EDIT_POI_ACTIVITY_CODE);
                } else {
                    // adding a note
                    NoteCommentDialogFragment dialog = NoteCommentDialogFragment.newInstance(pos.getLatitude(), pos.getLongitude());
                    dialog.show(getActivity().getFragmentManager(), "dialog");
                }
                break;

            case POI_POSITION_EDITION:
                newPoiPosition = mapView.getCenter();
                eventBus.post(new PleaseApplyPoiPositionChange(newPoiPosition, markerSelected.getPoi().getId()));
                markerSelected.setPoint(newPoiPosition);
                markerSelected.getPoi().setUpdated(true);
                mapView.addMarker(markerSelected);
                switchMode(MapMode.DETAIL_POI);
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Category.Edition.getValue())
                        .setAction("POI position edited")
                        .build());
                break;

            case NODE_REF_POSITION_EDITION:
                newPoiPosition = mapView.getCenter();
                eventBus.post(new PleaseApplyNodeRefPositionChange(newPoiPosition, markerSelected.getNodeRef().getId()));
                markerSelected.setPoint(newPoiPosition);
                switchMode(MapMode.WAY_EDITION);
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Category.Edition.getValue())
                        .setAction("way point position edited")
                        .build());
                break;

            case DETAIL_POI:
                getActivity().finish();
                break;

            default:
                break;
        }
    }

    //if is back will show the back arrow, else will display the menu icon
    private void toggleBackButton(boolean homeActionBtnMode) {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            this.homeActionBtnMode = homeActionBtnMode;
            if (homeActionBtnMode) {
                actionBar.setHomeAsUpIndicator(R.drawable.menu);
            } else {
                actionBar.setHomeAsUpIndicator(R.drawable.back);
            }

        }
    }

    private void displayHomeButton(boolean show) {
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        if (appCompatActivity != null) {
            ActionBar actionBar = appCompatActivity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(show);
            }
        }
    }

    public void onEventMainThread(OnBackPressedMapEvent event) {
        Timber.d("Received event OnBackPressedMap");
        if (isTuto) {
            closeTuto();
        } else {
            switch (mapMode) {
                case CREATION:
                    switchMode(MapMode.DEFAULT);
                    break;

                case POI_POSITION_EDITION:
                    mapView.addMarker(markerSelected);
                    switchMode(MapMode.DEFAULT);
                    break;


                case NODE_REF_POSITION_EDITION:
                    switchMode(MapMode.WAY_EDITION);
                    break;

                case WAY_EDITION:
                    clearVectorialEdition();
                    switchMode(MapMode.DEFAULT);
                    break;

                case DETAIL_POI:
                case DETAIL_NOTE:
                    switchMode(MapMode.DEFAULT);
                    break;

                default:
                    getActivity().finish();
                    break;
            }
        }
    }


    public MapMode getMapMode() {
        return mapMode;
    }

    public LocationMarker getMarkerSelected() {
        return markerSelected;
    }

    public void setMarkerSelected(LocationMarker markerSelected) {
        this.markerSelected = markerSelected;
    }

    public Long getMarkerSelectedId() {
        return markerSelectedId;
    }

    public void setMarkerSelectedId(Long markerSelectedId) {
        this.markerSelectedId = markerSelectedId;
    }

    public LocationMarker.MarkerType getSelectedMarkerType() {
        return selectedMarkerType;
    }

    public BitmapHandler getBitmapHandler() {
        return bitmapHandler;
    }

    private void switchMode(MapMode mode) {

        mapMode = mode;
        Bitmap bitmap;

        switchToolbarMode(mapMode);
        editNodeRefPosition.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        MapMode.MapModeProperties properties = mode.getProperties();

        if (properties.isUnSelectIcon()) {
            unselectIcon();
        }

        showFloatingButtonAddPoi(properties.isShowAddPoiFab());

        if (properties.isShowSpinner()) {
            ViewAnimation.animate(spinnerWrapper, true);
        } else {
            spinnerWrapper.setVisibility(View.GONE);
        }

        displayPoiDetailBanner(properties.isShowPoiBanner());
        displayNoteDetailBanner(properties.isShowNodeBanner());

        mapView.setMinZoomLevel(properties.isZoomOutLimited() ? zoomVectorial : 1);

        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(Category.MapMode.getValue())
                .setAction(properties.getAnalyticsAction())
                .build());

        switch (mode) {

            case DETAIL_POI:
            case DETAIL_NOTE:
                break;

            case CREATION:
                animationPoiCreation();
                break;

            case POI_POSITION_EDITION:
                // This marker is being moved
                bitmap = bitmapHandler.getMarkerBitmap(markerSelected.getPoi().getType().getId(), Poi.computeState(false, true, false));
                creationPin.setImageBitmap(bitmap);
                break;

            case NODE_REF_POSITION_EDITION:
                break;

            case WAY_EDITION:
                loadAreaForEdition();
                if (vectorialOverlay != null) {
                    vectorialOverlay.setMovingObjectId(null);
                    vectorialOverlay.setSelectedObjectId(null);
                } else if (markerSelected == null && markerSelectedId != -1) {
                    eventBus.post(new PleaseSelectNodeRefByID(markerSelectedId));
                }
                break;

            default:
                floatingMenuAddFewValues.collapse();
                break;
        }

        //the marker is displayed at the end of the animation
        creationPin.setVisibility(properties.isShowCreationPin() ? View.VISIBLE : View.GONE);

    }

    private void switchToolbarMode(MapMode mode) {
        MapMode.MapModeProperties properties = mode.getProperties();

        confirm.setVisible(properties.isShowConfirmBtn());
        downloadArea.setVisible(properties.isShowDownloadArea());
        filter.setVisible(!properties.isLockDrawer());
        toggleBackButton(properties.isMenuBtn());
        getActivity().setTitle(properties.getTitle(getActivity()));
        eventBus.post(new PleaseChangeToolbarColor(properties.isEditColor()));
        eventBus.post(new PleaseToggleDrawerLock(properties.isLockDrawer()));
    }

    public void onEventMainThread(PleaseSwitchModeEvent event) {
        switchMode(event.getMapMode());
        downloadAreaForEdition();
    }

    private void defaultMap() {
        presenter.setForceRefreshPoi();
        presenter.setForceRefreshNotes();
        presenter.loadPoisIfNeeded();
        switchMode(MapMode.DEFAULT);
    }

    private void unselectIcon() {
        if (markerSelected != null) {
            Bitmap bitmap = null;

            switch (markerSelected.getType()) {
                case POI:
                    bitmap = bitmapHandler.getMarkerBitmap(markerSelected.getPoi().getType().getId(), Poi.computeState(false, false, markerSelected.getPoi().getUpdated()));
                    break;

                case NOTE:
                    bitmap = bitmapHandler.getNoteBitmap(Note.computeState(markerSelected.getNote(), false, false));
                    break;

                case NODE_REF:
                    mapView.removeMarker(markerSelected);
                    break;

                default:
                    break;
            }
            if (bitmap != null) {
                markerSelected.setIcon(new Icon(new BitmapDrawable(getResources(), bitmap)));
            }
            markerSelected = null;
        }
    }

    public BoundingBox getViewBoundingBox() {
        return mapView.getBoundingBox();
    }

    public float getZoomLevel() {
        return mapView.getZoomLevel();
    }

    public boolean hasMarkers() {
        return !markersPoi.isEmpty();
    }

    public void removeAllMarkers() {
        for (Long markerId : markersNotes.keySet()) {
            removeMarker(markersNotes.get(markerId));
        }
        for (Long markerId : markersNodeRef.keySet()) {
            removeMarker(markersNodeRef.get(markerId));
        }
        markersNotes.clear();
        markersNodeRef.clear();
        removeAllPoiMarkers();
    }

    public void removeAllPoiMarkers() {
        for (Long markerId : markersPoi.keySet()) {
            removeMarker(markersPoi.get(markerId));
        }
        markersPoi.clear();
    }

    public void removePoiMarkersNotIn(List<Long> poiIds) {
        Set<Long> idsToRemove = new HashSet<>(markersPoi.keySet());
        idsToRemove.removeAll(poiIds);
        for (Long id : idsToRemove) {
            removeMarker(markersPoi.get(id));
        }
        markersPoi.keySet().removeAll(idsToRemove);
    }

    public Map<Long, LocationMarker> getMarkersPoi() {
        return markersPoi;
    }

    private void removeMarker(LocationMarker marker) {
        if (marker != null) {
            mapView.removeMarker(marker);
            Object poi = marker.getRelatedObject();
        }
    }

    public void reselectMarker() {
        // if I found the marker selected I click on it
        if (markerSelected != null) {
            if (isMenuLoaded) {
                onMarkerClick(markerSelected);
            } else {
                pleaseSwitchToPoiSelected = true;
            }
        }
    }

    public void addMarker(LocationMarker marker) {
        markersPoi.put(marker.getPoi().getId(), marker);
        addPoiMarkerDependingOnFilters(marker);
    }

    public void invalidateMap() {
        mapView.invalidate();
    }

    public void addNote(LocationMarker marker) {
        markersNotes.put(marker.getNote().getId(), marker);
        // add the note to the map
        addNoteMarkerDependingOnFilters(marker);
    }

    public LocationMarker getNote(Long id) {
        return markersNotes.get(id);
    }

    /*-----------------------------------------------------------
    * WAY EDITION
    *---------------------------------------------------------*/

    @InjectView(R.id.edit_way_elemnt_position)
    FloatingActionButton editNodeRefPosition;


    @OnClick(R.id.edit_way_elemnt_position)
    public void setEditNodeRefPosition() {
        vectorialOverlay.setMovingObjectId(markerSelected.getNodeRef().getNodeBackendId());
        switchMode(MapMode.NODE_REF_POSITION_EDITION);
    }

    private List<Overlay> overlays = new ArrayList<>();
    Set<VectorialObject> vectorialObjectsEdition = new HashSet<>();

    public boolean isVectorialObjEditionLoaded() {
        return vectorialObjectsEdition.isEmpty();
    }

    public void onDownloadZoneClick() {
        downloadAreaForEdition();
    }

    //get data from overpass
    private void downloadAreaForEdition() {
        if (getZoomLevel() >= zoomVectorial) {
            progressBar.setVisibility(View.VISIBLE);
            BoundingBox viewBoundingBox = getViewBoundingBox();
            eventBus.post(new SyncDownloadWayEvent(viewBoundingBox));
        } else {
            Toast.makeText(getActivity(), getString(R.string.zoom_to_edit), Toast.LENGTH_SHORT).show();
        }
    }

    //get data from the bd
    private void loadAreaForEdition() {
        if (getZoomLevel() >= zoomVectorial) {
            BoundingBox viewBoundingBox = getViewBoundingBox();
            eventBus.post(new PleaseLoadEditVectorialTileEvent(false));
        } else {
            Toast.makeText(getActivity(), getString(R.string.zoom_to_edit), Toast.LENGTH_SHORT).show();
        }
    }

    private void clearAllNodeRef() {
        for (LocationMarker locationMarker : markersNodeRef.values()) {
            removeMarker(locationMarker);
        }
        markersNodeRef.clear();
        for (Overlay overlay : overlays) {
            mapView.removeOverlay(overlay);
        }
    }

    public void onEventMainThread(NodeRefAroundLoadedEvent event) {
        List<PoiNodeRef> poiNodeRefsSelected = event.getPoiNodeRefs();
        if (poiNodeRefsSelected != null && poiNodeRefsSelected.size() > 0) {
            //todo let the user precise his choice
            //lets says it the first one
            if (markerSelected != null) {
                removeMarker(markerSelected);
            }
            unselectIcon();
            PoiNodeRef poiNodeRef = poiNodeRefsSelected.get(0);
            LocationMarker locationMarker = new LocationMarker(poiNodeRef);
            markerSelected = locationMarker;
            vectorialOverlay.setSelectedObjectId(poiNodeRef.getNodeBackendId());
            onNodeRefClick(locationMarker);
            mapView.invalidate();

        } else {
            unselectNoderef();
        }
    }

    private void unselectNoderef() {
        vectorialOverlay.setSelectedObjectId(null);
        markerSelected = null;
        markerSelectedId = -1L;
        editNodeRefPosition.setVisibility(View.GONE);
        mapView.invalidate();
    }

    /*-----------------------------------------------------------
    * POI CREATION
    *---------------------------------------------------------*/

    @InjectView(R.id.add_poi)
    FloatingActionButton floatingButtonAddPoi;

    @InjectView(R.id.hand)
    ImageView handImageView;

    @InjectView(R.id.add_poi_few_values)
    FloatingActionsMenu floatingMenuAddFewValues;

    @InjectView(R.id.poi_type_spinner)
    Spinner spinner;

    private SpinnerAdapter spinnerAdapter;

    @InjectView(R.id.poi_type_spinner_wrapper)
    RelativeLayout spinnerWrapper;

    @InjectView(R.id.floating_btn_wrapper)
    RelativeLayout floatingBtnWrapper;

    @InjectView(R.id.pin)
    ImageButton creationPin;

    private void animationPoiCreation() {
        handImageView.setVisibility(View.VISIBLE);
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, OsmCreationAnimatorUpdateListener.STEPS_CENTER_ANIMATION);
        valueAnimator.setDuration(1000);
        valueAnimator.addListener(new OsmCreationAnimatorUpdateListener(mapView, handImageView, getActivity()));
        valueAnimator.addUpdateListener(new OsmCreationAnimatorUpdateListener(mapView, handImageView, getActivity()));
        valueAnimator.start();
    }

    public void onEventMainThread(NewPoiTypeSelected event) {
        Timber.d("Received event NewPoiTypeSelected");
        PoiType poiType = (PoiType) spinner.getSelectedItem();
        poiTypeSelected(poiType);
    }

    private void poiTypeSelected(PoiType poiType) {
        poiTypeSelected = poiType;
        Bitmap bitmap;

        if (poiType != null) {
            bitmap = bitmapHandler.getMarkerBitmap(poiType.getId(), Poi.computeState(false, true, false));
            if (poiTypeHidden.contains(poiType.getId())) {
                poiTypeHidden.remove(poiType.getId());
                applyPoiFilter();
            }
        } else { // Comment type has been selected
            bitmap = bitmapHandler.getNoteBitmap(Note.computeState(null, false, true));

        }

        creationPin.setImageBitmap(bitmap);
        creationPin.setVisibility(View.VISIBLE);
    }

    private void showFloatingButtonAddPoi(boolean show) {
        //animation show and hide
        if (show) {
            if (floatingBtnWrapper.getVisibility() == View.GONE) {
                Animation bottomUp = AnimationUtils.loadAnimation(getActivity(),
                        R.anim.anim_up_detail);

                floatingBtnWrapper.startAnimation(bottomUp);
                floatingBtnWrapper.setVisibility(View.VISIBLE);
            }
        } else {
            if (floatingBtnWrapper.getVisibility() == View.VISIBLE) {
                Animation bottomUp = AnimationUtils.loadAnimation(getActivity(),
                        R.anim.anim_down_detail);

                floatingBtnWrapper.startAnimation(bottomUp);
                floatingBtnWrapper.setVisibility(View.GONE);
            }
        }

        //which kind of add to display depending on screen size
        if (presenter.getNumberOfPoiTypes() <= maxPoiType) {
            floatingMenuAddFewValues.setVisibility(View.VISIBLE);
            floatingMenuAddFewValues.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isTuto) {
                        nextTutoStep();
                    }
                }
            });
            floatingButtonAddPoi.setVisibility(View.INVISIBLE);
        } else {
            floatingButtonAddPoi.setVisibility(View.VISIBLE);
            floatingMenuAddFewValues.setVisibility(View.GONE);
        }
    }

    protected void loadPoiTypeFloatingBtn() {
        floatingMenuAddFewValues.removeAllButtons();
        FloatingActionButton floatingActionButton;

        //add note
        if (!FlavorUtils.isPoiStorage()) {
            floatingActionButton = new FloatingActionButton(getActivity());
            floatingActionButton.setTitle(getString(R.string.note));
            floatingActionButton.setColorPressed(getResources().getColor(R.color.material_green_700));
            floatingActionButton.setColorNormal(getResources().getColor(R.color.material_green_500));
            floatingActionButton.setSize(FloatingActionButton.SIZE_MINI);
            floatingActionButton.setIconDrawable(bitmapHandler.getIconWhite(null));
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switchMode(MapMode.CREATION);
                    //the note is the last one
                    spinner.setSelection(spinner.getCount() - 1);
                    poiTypeSelected(null);
                    floatingMenuAddFewValues.collapse();
                    if (isTuto) {
                        nextTutoStep();
                    }
                }
            });
            floatingMenuAddFewValues.addButton(floatingActionButton);
        }

        //add poiTypes
        for (final PoiType poiType : presenter.getPoiTypes()) {
            floatingActionButton = new FloatingActionButton(getActivity());
            floatingActionButton.setTitle(poiType.getName());
            floatingActionButton.setColorPressed(getResources().getColor(R.color.material_blue_grey_800));
            floatingActionButton.setColorNormal(getResources().getColor(R.color.material_blue_500));
            floatingActionButton.setSize(FloatingActionButton.SIZE_MINI);
            floatingActionButton.setIconDrawable(bitmapHandler.getIconWhite(poiType.getId()));
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (configManager.hasPoiAddition()) {
                        switchMode(MapMode.CREATION);
                        int pos = ((SpinnerAdapter) spinner.getAdapter()).getPoiTypePosition(poiType.getId());
                        spinner.setSelection(pos);
                        poiTypeSelected(poiType);
                        floatingMenuAddFewValues.collapse();
                        if (isTuto) {
                            nextTutoStep();
                        }
                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.point_modification_forbidden), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            floatingMenuAddFewValues.addButton(floatingActionButton);
        }
    }

    protected void loadPoiTypeSpinner() {
        if (spinnerAdapter == null) {
            spinnerAdapter = new SpinnerAdapter(getActivity(), bitmapHandler);
            spinner.setAdapter(spinnerAdapter);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    eventBus.post(new NewPoiTypeSelected());
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }

        //like this the only object the user can create will be the note
        if (configManager.hasPoiAddition()) {
            spinnerAdapter.clear();
            spinnerAdapter.addItems(presenter.getPoiTypes());
        }
        //if there is only one poitype disable the spinner
        if (spinnerAdapter.getCount() == 1) {
            spinner.setEnabled(false);
        } else {
            spinner.setEnabled(true);
        }
        spinnerAdapter.notifyDataSetChanged();

    }

    @OnClick(R.id.add_poi)
    public void setOnAddPoi() {
        poiTypeSelected(poiTypeSelected);
        switchMode(MapMode.CREATION);
        if (isTuto) {
            nextTutoStep();
        }
    }

    /*-----------------------------------------------------------
    * POI EDITION
    *---------------------------------------------------------*/

    public void onEventMainThread(PleaseOpenEditionEvent event) {
        Timber.d("Received event PleaseOpenEdition");
        Intent intent = new Intent(getActivity(), EditPoiActivity.class);
        intent.putExtra(EditPoiActivity.CREATION_MODE, false);
        intent.putExtra(EditPoiActivity.POI_ID, markerSelected.getPoi().getId());
        startActivity(intent);
    }

    public void onEventMainThread(PleaseChangePoiPosition event) {
        Timber.d("Received event PleaseChangePoiPosition");
        if (configManager.hasPoiModification()) {
            switchMode(MapMode.POI_POSITION_EDITION);
            creationPin.setVisibility(View.GONE);

            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, OsmAnimatorUpdateListener.STEPS_CENTER_ANIMATION);
            valueAnimator.setDuration(900);
            valueAnimator.addUpdateListener(new OsmAnimatorUpdateListener(mapView.getCenter(), markerSelected.getPoint(), mapView));

            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    creationPin.setVisibility(View.VISIBLE);
                    removeMarker(markerSelected);
                }
            });

            valueAnimator.start();
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.point_modification_forbidden), Toast.LENGTH_SHORT).show();
        }
    }

    /*-----------------------------------------------------------
    * POI DELETION
    *---------------------------------------------------------*/

    public void onEventMainThread(PleaseDeletePoiFromMapEvent event) {
        Poi poi = markerSelected.getPoi();
        poi.setToDelete(true);
        markersPoi.remove(poi.getId());
        removeMarker(markerSelected);
        eventBus.post(new PleaseDeletePoiEvent(poi));
        switchMode(MapMode.DEFAULT);
    }

    /*-----------------------------------------------------------
    * POI DETAIL
    *---------------------------------------------------------*/
    private void displayPoiDetailBanner(boolean display) {
        if (display) {

            if (markerSelected != null) {
                eventBus.post(new PleaseChangeValuesDetailPoiFragmentEvent(presenter.getPoiType(markerSelected.getPoi().getType().getId()).getName(), markerSelected.getPoi().getName()));
            }

            if (poiDetailWrapper.getVisibility() != View.VISIBLE) {
                Animation bottomUp = AnimationUtils.loadAnimation(getActivity(),
                        R.anim.anim_up_detail);

                poiDetailWrapper.startAnimation(bottomUp);
                poiDetailWrapper.setVisibility(View.VISIBLE);
            }

        } else {


            if (poiDetailWrapper.getVisibility() == View.VISIBLE) {
                Animation bottomUp = AnimationUtils.loadAnimation(getActivity(),
                        R.anim.anim_down_detail);

                poiDetailWrapper.startAnimation(bottomUp);
                poiDetailWrapper.setVisibility(View.GONE);
            }
        }
    }

    /*-----------------------------------------------------------
    * NOTE DETAIL
    *---------------------------------------------------------*/
    private void displayNoteDetailBanner(boolean display) {
        if (display) {
            if (markerSelected != null && !markerSelected.isPoi()) {
                eventBus.post(new PleaseChangeValuesDetailNoteFragmentEvent(markerSelected.getNote()));
            }

            if (noteDetailWrapper.getVisibility() != View.VISIBLE) {
                Animation bottomUp = AnimationUtils.loadAnimation(getActivity(),
                        R.anim.anim_up_detail);

                noteDetailWrapper.startAnimation(bottomUp);
                noteDetailWrapper.setVisibility(View.VISIBLE);
            }

        } else {


            if (noteDetailWrapper.getVisibility() == View.VISIBLE) {
                Animation bottomUp = AnimationUtils.loadAnimation(getActivity(),
                        R.anim.anim_down_detail);

                noteDetailWrapper.startAnimation(bottomUp);
                noteDetailWrapper.setVisibility(View.GONE);
            }
        }
    }

    /*-----------------------------------------------------------
    * MAP UTILS
    *---------------------------------------------------------*/
    @InjectView(R.id.localisation)
    FloatingActionButton floatingButtonLocalisation;

    @OnClick(R.id.localisation)
    public void setOnPosition() {
        Timber.d("Center position on user location");
        LatLng pos = mapView.getUserLocation();
        if (pos != null) {
            mapView.setCenter(pos);
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.location_not_found), Toast.LENGTH_SHORT).show();
        }
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(Category.GeoLocation.getValue())
                .setAction("Center map on user geolocation")
                .build());
    }


    /*-----------------------------------------------------------
    * SYNC
    *---------------------------------------------------------*/

    public void onEventMainThread(SyncFinishUploadPoiEvent event) {
        String result;

        if (event.getSuccessfullyAddedPoisCount() > 0) {
            result = String.format(getResources().getString(R.string.add_done), event.getSuccessfullyAddedPoisCount());
            presenter.setForceRefreshPoi();
            Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
        }
        if (event.getSuccessfullyUpdatedPoisCount() > 0) {
            if (mapMode == MapMode.WAY_EDITION) {
                result = String.format(getResources().getString(R.string.noderef_moved), event.getSuccessfullyUpdatedPoisCount());
            } else {
                result = String.format(getResources().getString(R.string.update_done), event.getSuccessfullyUpdatedPoisCount());
                presenter.setForceRefreshPoi();
            }
            Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
            Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
            presenter.setForceRefreshPoi();

        }
        if (event.getSuccessfullyDeletedPoisCount() > 0) {
            result = String.format(getResources().getString(R.string.delete_done), event.getSuccessfullyDeletedPoisCount());
            Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
            presenter.setForceRefreshPoi();
        }

        if (presenter.isForceRefreshPoi()) {
            presenter.loadPoisIfNeeded();
        }
    }


    public void onEventMainThread(SyncFinishUploadNote event) {
        presenter.setForceRefreshNotes();
        presenter.loadPoisIfNeeded();
    }

    public void onEventMainThread(NewNoteCreatedEvent event) {
        // a note has been created, select it
        markerSelectedId = event.getNoteId();
        markerSelected = null;
        selectedMarkerType = LocationMarker.MarkerType.NOTE;
        presenter.setForceRefreshNotes();
        presenter.loadPoisIfNeeded();
    }

    public void onEventMainThread(SyncUnauthorizedEvent event) {
        Toast.makeText(getActivity(), R.string.couldnt_connect_retrofit, Toast.LENGTH_LONG).show();
    }

    public void onEventMainThread(SyncDownloadRetrofitErrorEvent event) {
        Toast.makeText(getActivity(), R.string.couldnt_download_retrofit, Toast.LENGTH_SHORT).show();
    }

    public void onEventMainThread(SyncConflictingNodeErrorEvent event) {
        Toast.makeText(getActivity(), R.string.couldnt_update_node, Toast.LENGTH_LONG).show();
        removePoiMarkerInError(event.getPoiIdInError());
    }

    public void onEventMainThread(SyncNewNodeErrorEvent event) {
        Toast.makeText(getActivity(), R.string.couldnt_create_node, Toast.LENGTH_LONG).show();
        removePoiMarkerInError(event.getPoiIdInError());
    }

    public void onEventMainThread(SyncUploadRetrofitErrorEvent event) {
        Toast.makeText(getActivity(), R.string.couldnt_upload_retrofit, Toast.LENGTH_SHORT).show();
        removePoiMarkerInError(event.getPoiIdInError());
    }

    public void onEventMainThread(SyncUploadNoteRetrofitErrorEvent event) {
        Toast.makeText(getActivity(), R.string.couldnt_upload_retrofit, Toast.LENGTH_SHORT).show();
        removeNoteMarkerInError(event.getNoteIdInError());
    }

    // markers were added on the map the sync failed we remove them
    private void removePoiMarkerInError(Long id) {
        Marker m = markersPoi.remove(id);
        if (m != null) {
            mapView.removeMarker(m);
        }
        defaultMap();
    }

    // note was added on the map the sync failed we remove it
    private void removeNoteMarkerInError(Long id) {
        Marker m = markersNotes.remove(id);
        if (m != null) {
            mapView.removeMarker(m);
        }
        defaultMap();
    }

    /*-----------------------------------------------------------
    * VECTORIAL
    *---------------------------------------------------------*/

    @InjectView(R.id.level_bar)
    LevelBar levelBar;

    private BoundingBox triggerReloadVectorialTiles;
    private VectorialOverlay vectorialOverlay;
    private boolean isVectorial = false;
    private double currentLevel = 0;
    Set<VectorialObject> vectorialObjectsBackground = new HashSet<>();
    private LocationMarker.MarkerType selectedMarkerType = LocationMarker.MarkerType.NONE;
    private int zoomVectorial;


    public void onEventMainThread(VectorialTilesLoadedEvent event) {
        Timber.d("Received event VectorialTilesLoaded");
        vectorialObjectsBackground.clear();
        vectorialObjectsBackground = event.getVectorialObjects();
        updateVectorial(event.getLevels());
    }

    public void onEventMainThread(EditionVectorialTilesLoadedEvent event) {
        if (event.isRefreshFromOverpass()) {
            progressBar.setVisibility(View.GONE);
        }
        if (mapMode == MapMode.WAY_EDITION) {
            Timber.d("Showing nodesRefs : " + event.getVectorialObjects().size());
            clearAllNodeRef();
            vectorialObjectsEdition.clear();
            vectorialObjectsEdition.addAll(event.getVectorialObjects());
            updateVectorial(event.getLevels());


            if (getMarkerSelected() != null && getMarkerSelected().isNodeRef()) {
                boolean reselectMaker = false;
                for (VectorialObject v : vectorialObjectsEdition) {
                    if (v.getId().equals(getMarkerSelected().getNodeRef().getNodeBackendId())) {
                        reselectMaker = true;
                    }
                }
                if (!reselectMaker) {
                    unselectNoderef();
                }
            }
        }
    }

    private void updateVectorial(TreeSet<Double> levels) {

        if (vectorialOverlay == null) {
            Set<VectorialObject> vectorialObjects = new HashSet<>();
            vectorialObjects.addAll(vectorialObjectsBackground);
            vectorialObjects.addAll(vectorialObjectsEdition);

            vectorialOverlay = new VectorialOverlay(zoomVectorial, vectorialObjects, levels);
            mapView.addOverlay(vectorialOverlay);
        } else {

            Set<VectorialObject> vectorialObjects = new HashSet<>();
            vectorialObjects.addAll(vectorialObjectsBackground);
            vectorialObjects.addAll(vectorialObjectsEdition);

            vectorialOverlay.setVectorialObjects(vectorialObjects);
            vectorialOverlay.setLevels(levels);
        }
        invalidateMap();

        levelBar.setLevels(vectorialOverlay.getLevels(), currentLevel);
        if (levelBar.getLevels().length < 2) {
            levelBar.setVisibility(View.INVISIBLE);
        } else {
            levelBar.setVisibility(View.VISIBLE);
        }
    }

    private void clearVectorialEdition() {
        if (vectorialOverlay == null) {
            return;
        } else {
            vectorialObjectsEdition.clear();
            Set<VectorialObject> vectorialObjects = new HashSet<>();
            vectorialObjects.addAll(vectorialObjectsBackground);
            vectorialOverlay.setVectorialObjects(vectorialObjects);
            invalidateMap();
        }
        levelBar.setLevels(vectorialOverlay.getLevels(), currentLevel);
        if (levelBar.getLevels().length < 2) {
            levelBar.setVisibility(View.INVISIBLE);
        } else {
            levelBar.setVisibility(View.VISIBLE);
        }
    }

    public void onEventMainThread(TooManyRequestsEvent event) {
        progressBar.setVisibility(View.GONE);
    }


    /*-----------------------------------------------------------
    * ADDRESS
    *---------------------------------------------------------*/
    @InjectView(R.id.addressView)
    TextView addressView;

    @Inject
    Geocoder geocoder;

    public void onEventMainThread(AddressFoundEvent event) {
        Timber.d("Received event AddressFound");
        if (getZoomLevel() >= zoomVectorial) {
            addressView.setVisibility(View.VISIBLE);
        }
        addressView.setText(event.getAddress());
    }

    /*-----------------------------------------------------------
    * FILTERS
    *---------------------------------------------------------*/

    private List<Long> poiTypeHidden = new ArrayList<>();

    private boolean displayOpenNotes = true;
    private boolean displayClosedNotes = true;

    public List<Long> getPoiTypeHidden() {
        return poiTypeHidden;
    }

    public void onEventMainThread(PleaseApplyPoiFilter event) {
        Timber.d("filtering Pois by type");
        poiTypeHidden = event.getPoiTypeIdsToHide();
        applyPoiFilter();
    }

    public void onEventMainThread(PleaseApplyNoteFilterEvent event) {
        Timber.d("filtering Notes");
        displayOpenNotes = event.isDisplayOpenNotes();
        displayClosedNotes = event.isDisplayClosedNotes();
        applyNoteFilter();
    }

    private void applyNoteFilter() {
        for (LocationMarker marker : markersNotes.values()) {
            removeMarker(marker);
            addNoteMarkerDependingOnFilters(marker);
        }
    }

    private void applyPoiFilter() {
        for (LocationMarker marker : markersPoi.values()) {
            removeMarker(marker);
            addPoiMarkerDependingOnFilters(marker);
        }
    }

    private void addNoteMarkerDependingOnFilters(LocationMarker marker) {
        Note note = marker.getNote();

        if ((displayOpenNotes && Note.STATUS_OPEN.equals(note.getStatus())) || Note.STATUS_SYNC.equals(note.getStatus()) || (displayClosedNotes && Note.STATUS_CLOSE.equals(note.getStatus()))) {
            mapView.addMarker(marker);
        } else if (mapMode.equals(MapMode.DETAIL_NOTE) && markerSelected.getNote().getId().equals(note.getId())) {
            switchMode(MapMode.DEFAULT);
        }
    }

    private void addPoiMarkerDependingOnFilters(LocationMarker marker) {
        Poi poi = marker.getPoi();
        //if we are in vectorial mode we hide all poi not at the current level
        if (poi.getType() != null && !poiTypeHidden.contains(poi.getType().getId()) && (!isVectorial || poi.isAtLevel(currentLevel) || !poi.isOnLevels(levelBar.getLevels()))) {
            mapView.addMarker(marker);
        } else if (mapMode.equals(MapMode.DETAIL_POI) && markerSelected.getPoi().getId().equals(poi.getId())) {
            //if the poi selected is hidden close the detail mode
            switchMode(MapMode.DEFAULT);
        }
    }

    /*-----------------------------------------------------------
    * TUTORIAL
    *---------------------------------------------------------*/

    public static final String TUTORIAL_CREATION_FINISH = "TUTORIAL_CREATION_FINISH";
    private ShowcaseView showcaseView;
    private int showcaseCounter = 0;
    private boolean isTuto = false;

    public void onEventMainThread(PleaseDisplayTutorialEvent event) {
        switchMode(MapMode.DEFAULT);
        displayTutorial(true);
    }

    protected void displayTutorial(boolean forceDisplay) {
        showcaseCounter = 0;

        if (presenter.getNumberOfPoiTypes() < 1) {
            return;
        }

        if (!configManager.hasPoiAddition()) {
            sharedPreferences.edit().putBoolean(TUTORIAL_CREATION_FINISH, true).apply();
            return;
        }

        boolean showTuto = forceDisplay || !sharedPreferences.getBoolean(TUTORIAL_CREATION_FINISH, false);

        if (showTuto && !isTuto) {
            isTuto = true;
            //position OK button on the left
            RelativeLayout.LayoutParams params =
                    new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            params.setMargins(60, 0, 0, 200);

            showcaseView = new ShowcaseView.Builder(getActivity(), true)
                    .setStyle(R.style.CustomShowcaseTheme)
                    .setContentTitle(getString(R.string.tuto_title_press_create))
                    .setContentText(getString(R.string.tuto_text_press_create))
                    .setTarget(new ViewTarget(floatingButtonAddPoi))
                    .setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                switch (showcaseCounter) {
                                                    case 0:
                                                        if (presenter.getNumberOfPoiTypes() <= maxPoiType) {
                                                            floatingMenuAddFewValues.expand();
                                                            nextTutoStep();
                                                        } else {
                                                            floatingButtonAddPoi.performClick();
                                                        }
                                                        break;

                                                    case 1:
                                                        if (presenter.getNumberOfPoiTypes() <= maxPoiType) {
                                                            floatingMenuAddFewValues.getChildAt(0).performClick();
                                                        } else {
                                                            nextTutoStep();
                                                        }
                                                        break;

                                                    case 2:
                                                        nextTutoStep();
                                                        break;

                                                    case 3:
                                                        nextTutoStep();
                                                        switchMode(MapMode.DEFAULT);
                                                        break;
                                                }
                                            }
                                        }
                    )
                    .build();

            showcaseView.setButtonPosition(params);
        }
    }

    private void nextTutoStep() {
        switch (showcaseCounter) {
            case 0:
                View objectToFocus = presenter.getNumberOfPoiTypes() <= maxPoiType ? floatingMenuAddFewValues.getChildAt(0) : spinner;
                showcaseView.setContentText(getString(R.string.tuto_text_choose_type));
                showcaseView.setTarget(new ViewTarget(objectToFocus));
                break;


            case 1:
                showcaseView.setContentText(getString(R.string.tuto_text_swipe_for_position));
                showcaseView.setTarget(new ViewTarget(mapView));
                break;

            case 2:
                showcaseView.setContentText(getString(R.string.tuto_text_confirm_position_creation));
                showcaseView.setTarget(new ViewTarget(R.id.action_confirm_position, getActivity()));
                break;

            case 3:
                closeTuto();
                break;
        }

        showcaseCounter++;
    }

    private void closeTuto() {
        showcaseView.hide();
        isTuto = false;
        sharedPreferences.edit().putBoolean(TUTORIAL_CREATION_FINISH, true).apply();
    }
}