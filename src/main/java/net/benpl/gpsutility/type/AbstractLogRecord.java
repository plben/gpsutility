/*
 * Copyright 2018 Ben Peng
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package net.benpl.gpsutility.type;

import net.benpl.gpsutility.gpx.WptType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * Abstract type of log record to store GPS information of each way point.
 * You may inherit this class to implement the decode logic and store decoded data to this object.
 */
abstract public class AbstractLogRecord {

    /**
     * When this record occurred. (in UTC format)
     */
    protected Date utc;

    /**
     * Record valid info. (MTK specific???)
     * <p>
     * BIT[0]: NO FIX or invalid
     * BIT[1]: SPS mode
     * BIT[2] : DGPS mode
     * BIT[3] : PPS mode
     * BIT[4] : RTK
     * BIT[5] : FRTK
     * BIT[6]: Estimated mode
     * BIT[7]: Manual input mode
     * BIT[8]: Simulator Mode
     */
    protected Integer valid;

    /**
     * Value in degree. North latitude will be positive value and the south
     * latitudewill be a negative value.
     */
    protected Double latitude;

    /**
     * Value in degree. East longitude will be positive value and the west
     * latitudewill be a negative value.
     */
    protected Double longitude;

    /**
     * Value in meters. (WGS-84)
     */
    protected Double height;

    /**
     * Value in Km / hour.
     */
    protected Double speed;

    /**
     * Value in degrees.
     */
    protected Double heading;

    /**
     * Differential GPS reference station ID.
     */
    protected Integer dsta;

    /**
     * Differential GPS correction data age.
     */
    protected Double dage;

    /**
     * PDOP * 100. (Position Dilution of Precision)
     */
    protected Integer pdop;

    /**
     * HDOP * 100. (Horizontal Dilution of Precision)
     */
    protected Integer hdop;

    /**
     * VDOP * 100. (Vertical Dilution of Precision)
     */
    protected Integer vdop;

    /**
     * Number of satellites in view.
     */
    protected Integer nsatInView;

    /**
     * Number of satellites in use.
     */
    protected Integer nsatInUsed;

    /**
     * Number of satellites in view. (duplicated with {@link #nsatInView}???)
     */
    protected Integer satCount;

    /**
     * Satellites information.
     */
    protected SatInfo[] sats = new SatInfo[32];

    /**
     * Record Reason. (MTK specific???)
     * <p>
     * BIT[0]􀃎 Recorded by the time criteria
     * BIT[1]􀃎 Record by the speed criteria
     * BIT[2]􀃎 Record by the distance criteria
     * BIT[3]􀃎 Record by Button
     * BIT[15:4]􀃎Vendor usage
     */
    protected Integer rcr;

    /**
     * Milliseconds.
     * The milliseconds part of the current recording time. The second part of the
     * current time should consult to {@link #utc}.
     */
    protected Integer milliseconds;

    /**
     * Distance (0.1 meter) since last/first waypoint.
     */
    protected Double distance;

    public Date getUtc() {
        return utc;
    }

    public void setUtc(Date utc) {
        this.utc = utc;
    }

    public Integer getValid() {
        return valid;
    }

    public void setValid(Integer valid) {
        this.valid = valid;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Double getHeading() {
        return heading;
    }

    public void setHeading(Double heading) {
        this.heading = heading;
    }

    public Integer getDsta() {
        return dsta;
    }

    public void setDsta(Integer dsta) {
        this.dsta = dsta;
    }

    public Double getDage() {
        return dage;
    }

    public void setDage(Double dage) {
        this.dage = dage;
    }

    public Integer getPdop() {
        return pdop;
    }

    public void setPdop(Integer pdop) {
        this.pdop = pdop;
    }

    public Integer getHdop() {
        return hdop;
    }

    public void setHdop(Integer hdop) {
        this.hdop = hdop;
    }

    public Integer getVdop() {
        return vdop;
    }

    public void setVdop(Integer vdop) {
        this.vdop = vdop;
    }

    public Integer getNsatInView() {
        return nsatInView;
    }

    public void setNsatInView(Integer nsatInView) {
        this.nsatInView = nsatInView;
    }

    public Integer getNsatInUsed() {
        return nsatInUsed;
    }

    public void setNsatInUsed(Integer nsatInUsed) {
        this.nsatInUsed = nsatInUsed;
    }

    public Integer getSatCount() {
        return satCount;
    }

    public void setSatCount(Integer satCount) {
        this.satCount = satCount;
    }

    public SatInfo[] getSats() {
        return sats;
    }

    public void setSats(SatInfo[] sats) {
        this.sats = sats;
    }

    public Integer getRcr() {
        return rcr;
    }

    public void setRcr(Integer rcr) {
        this.rcr = rcr;
    }

    public Integer getMilliseconds() {
        return milliseconds;
    }

    public void setMilliseconds(Integer milliseconds) {
        this.milliseconds = milliseconds;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    /**
     * Convert way point record to GPX format. (GPX 1.1 compliant)
     *
     * @param wpId The sequence number to name this way point.
     * @return GPX way point.
     */
    public WptType toGpxWpt(int wpId) {
        WptType wpt = new WptType();

        // GPX:Required Information
        if (latitude != null) {
            wpt.setLat(BigDecimal.valueOf(latitude));
        }

        // GPX:Required Information
        if (longitude != null) {
            wpt.setLon(BigDecimal.valueOf(longitude));
        }

        // GPX:Optional Position Information
        if (utc != null) {
            wpt.setTime(new Date(utc.getTime() + (milliseconds == null ? 0 : milliseconds)));
        }

        // GPX:Optional Position Information
        if (height != null) {
            wpt.setEle(BigDecimal.valueOf(height));
        }

        // GPX:Optional Accuracy Information
        if (valid != null) {
            if ((valid & 0x01) != 0) {
                wpt.setFix("none");
            } else if ((valid & 0x04) != 0) {
                wpt.setFix("dgps");
            } else if ((valid & 0x08) != 0) {
                wpt.setFix("pps");
            } else if (height != null) {
                wpt.setFix("3d");
            } else {
                wpt.setFix("2d");
            }
        }

        // GPX:Optional Accuracy Information
        // Number of satellites used to calculate the GPS fix. (not number of satellites in view).
        if (dage != null) {
            wpt.setSat(BigInteger.valueOf(nsatInUsed));
        }

        // GPX:Optional Accuracy Information
        if (hdop != null) {
            wpt.setHdop(BigDecimal.valueOf(hdop / 100.0));
        }

        // GPX:Optional Accuracy Information
        if (vdop != null) {
            wpt.setVdop(BigDecimal.valueOf(vdop / 100.0));
        }

        // GPX:Optional Accuracy Information
        if (pdop != null) {
            wpt.setPdop(BigDecimal.valueOf(pdop / 100.0));
        }

        // GPX:Optional Accuracy Information
        if (dage != null) {
            wpt.setAgeofdgpsdata(BigDecimal.valueOf(dage));
        }

        // GPX:Optional Accuracy Information
        if (dsta != null) {
            wpt.setDgpsid(dsta);
        }

        // GPX:Optional Description Information
        wpt.setName(String.format("TP%06d", wpId));

        return wpt;
    }

    public static class SatInfo {

        /**
         * ID of satellite in view.
         */
        public int id;

        /**
         * SAT in use.
         */
        public boolean used;

        /**
         * Elevation angle in degree of the SID.
         */
        public Integer elevation;

        /**
         * Azimuth angle in degree of the SID.
         */
        public Integer azimut;

        /**
         * SNR of the SID.
         */
        public Integer snr;
    }

}
