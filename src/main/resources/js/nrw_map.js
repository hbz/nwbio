import "scss/map.scss";
import { geoMercator, geoPath } from "d3-geo";
import { feature } from "topojson-client";

/*
 * Interactive map of NRW districts (Kreise) and municipalities (Gemeinden),
 * ported from nwbib (app/views/tags/nrw_map.scala.html).
 *
 * Clicking a district/municipality starts a location-based search, using the
 * same URL scheme as nwbib: /search?location=<name>|<geohash+geohash+...>
 * (The backend support for the `location` parameter is not yet implemented.)
 */

/* District IDs of Westfalen, for highlighting (same list as in nwbib): */
var westfalen = [
    "05554",
    "05562",
    "05512",
    "05513",
    "05916",
    "05911",
    "05954",
    "05962",
    "05966",
    "05970",
    "05566",
    "05558",
    "05515",
    "05978",
    "05913",
    "05914",
    "05958",
    "05974",
    "05570",
    "05774",
    "05915",
    "05754",
    "05711",
    "05758",
    "05770",
    "05766",
    "05762",
];

function startWithWestfalen(id) {
    for (var i = 0; i < westfalen.length; i++) {
        if (id.indexOf(westfalen[i]) === 0) {
            return true;
        }
    }
    return false;
}

/* Encode latitude/longitude to a geohash (from Chris Veness' latlon-geohash.js, MIT): */
function geohashEncode(lat, lon, precision) {
    var base32 = "0123456789bcdefghjkmnpqrstuvwxyz";
    var idx = 0;
    var bit = 0;
    var evenBit = true;
    var geohash = "";
    var latMin = -90,
        latMax = 90,
        lonMin = -180,
        lonMax = 180;
    while (geohash.length < precision) {
        if (evenBit) {
            var lonMid = (lonMin + lonMax) / 2;
            if (lon > lonMid) {
                idx = idx * 2 + 1;
                lonMin = lonMid;
            } else {
                idx = idx * 2;
                lonMax = lonMid;
            }
        } else {
            var latMid = (latMin + latMax) / 2;
            if (lat > latMid) {
                idx = idx * 2 + 1;
                latMin = latMid;
            } else {
                idx = idx * 2;
                latMax = latMid;
            }
        }
        evenBit = !evenBit;
        if (++bit === 5) {
            geohash += base32.charAt(idx);
            bit = 0;
            idx = 0;
        }
    }
    return geohash;
}

/*
 * Flatten multiple nested polygons into a single polygon if required,
 * then encode each coordinate as geohash, joined by '+' (same as nwbib):
 */
function toGeohashes(coordinates) {
    var flat =
        typeof coordinates[0][0] === "number" ? coordinates : [].concat.apply([], coordinates);
    var result = "";
    for (var i = 0; i < flat.length; i++) {
        result += geohashEncode(flat[i][1], flat[i][0], 6) + "+";
    }
    return result.slice(0, -1);
}

function fillColor(value) {
    if (value.id === "05") {
        return "rgba(0, 0, 0, 0)";
    }
    return startWithWestfalen(value.id) ? "rgba(238, 25, 33, 0.0625)" : "rgba(0, 163, 76, 0.0625)";
}

function highlightFillColor(value) {
    if (value.id === "05") {
        return "rgba(0, 0, 0, 0)";
    }
    return startWithWestfalen(value.id) ? "rgba(238, 25, 33, 1)" : "rgba(0, 163, 76, 1)";
}

/* Hover tooltip with the district/municipality name, like datamaps in nwbib: */
function attachTooltip(map, element, name) {
    var tooltip = document.createElement("span");
    tooltip.className = "map-tooltip";
    tooltip.textContent = name;
    map.appendChild(tooltip);
    function move(event) {
        var rect = map.getBoundingClientRect();
        /* Offset below/right of the cursor so the pointer doesn't cover the tooltip: */
        tooltip.style.left = event.clientX - rect.left + 12 + "px";
        tooltip.style.top = event.clientY - rect.top + 14 + "px";
    }
    element.addEventListener("mouseenter", function (event) {
        tooltip.style.display = "block";
        move(event);
    });
    element.addEventListener("mousemove", move);
    element.addEventListener("mouseleave", function () {
        tooltip.style.display = "none";
    });
}

document.addEventListener("DOMContentLoaded", function () {
    var map = document.getElementById("map");
    if (!map) {
        return;
    }
    var scope = map.dataset.scope === "gemeinden" ? "gemeinden" : "kreise";

    var request = new XMLHttpRequest();
    request.open("GET", "/assets/data/nrw.topo.json", false);
    request.send();
    var topology;
    try {
        topology = JSON.parse(request.responseText);
    } catch (error) {
        console.error("Failed to load NRW map: " + error);
        return;
    }
    renderMap(map, topology, scope);
});

/* Fixed internal coordinate space, like the 400x400 box in nwbib; scaled via CSS: */
var MAP_SIZE = 450;

function renderMap(map, topology, scope) {
    /* The feature with id "05" is the whole-of-NRW outline (black border, no fill): */
    var features = feature(topology, topology.objects[scope]);
    var projection = geoMercator().fitExtent(
        [
            [0, 0],
            [MAP_SIZE, MAP_SIZE],
        ],
        features
    );
    var path = geoPath().projection(projection);
    var svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
    svg.setAttribute("class", "map-svg");
    svg.setAttribute("viewBox", "0 0 " + MAP_SIZE + " " + MAP_SIZE);
    map.appendChild(svg);

    for (var i = 0; i < features.features.length; i++) {
        (function (geometry) {
            var element = document.createElementNS("http://www.w3.org/2000/svg", "path");
            var isNrwOutline = geometry.id === "05";
            element.setAttribute("class", isNrwOutline ? "map-svg-nrw" : "map-svg-subunit");
            element.setAttribute("d", path(geometry));
            element.setAttribute("fill", isNrwOutline ? "none" : fillColor(geometry));
            if (!isNrwOutline) {
                if (geometry.properties && geometry.properties.name) {
                    attachTooltip(map, element, geometry.properties.name);
                }
                element.addEventListener("mouseenter", function () {
                    element.setAttribute("fill", highlightFillColor(geometry));
                });
                element.addEventListener("mouseleave", function () {
                    element.setAttribute("fill", fillColor(geometry));
                });
                element.addEventListener("click", function () {
                    if (geometry.properties && geometry.properties.name) {
                        var label = encodeURIComponent(geometry.properties.name);
                        var polygon = toGeohashes(geometry.geometry.coordinates[0]);
                        window.location = "/search?location=" + label + "|" + polygon;
                    }
                });
            }
            svg.appendChild(element);
        })(features.features[i]);
    }
}
