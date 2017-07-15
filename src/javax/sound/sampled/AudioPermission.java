/**
 * Copyright (c) 1999, 2002, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sound.sampled;

import java.security.BasicPermission;

/**
 * (OLD PERMISSIONS TAKEN OUT FOR 1.2 BETA)
 *
 * <tr>
 * <td>playback device access</td>
 * <td>Direct access to the audio playback device(s), including configuration of the
 * playback format, volume, and balance, explicit opening and closing of the device,
 * etc.</td>
 * <td>Changes the properties of a shared system device and therefore
 * can affect other applications.</td>
 * </tr>
 *
 * <tr>
 * <td>playback device override</td>
 * <td>Manipulation of the audio playback device(s) in a way that directly conflicts
 * with use by other applications.  This includes closing the device while it is in
 * use by another application, changing the device format while another application
 * is using it, etc. </td>
 * <td>Changes the properties of a shared system device and therefore
 * can affect other applications.</td>
 * </tr>
 *
 * <tr>
 * <td>record device access</td>
 * <td>Direct access to the audio recording device(s), including configuration of the
 * the record format, volume, and balance, explicit opening and closing of the device,
 * etc.</td>
 * <td>Changes the properties of a shared system device and therefore
 * can affect other applications.</td>
 * </tr>
 *
 * <tr>
 * <td>record device override</td>
 * <td>Manipulation of the audio recording device(s) in a way that directly conflicts
 * with use by other applications.  This includes closing the device while it is in
 * use by another application, changing the device format while another application
 * is using it, etc. </td>
 * <td>Changes the properties of a shared system device and therefore
 * can affect other applications.</td>
 * </tr>
 *
 * </table>
 *<p>
 *
 * @author Kara Kytle
 * @since 1.3
 */
public class AudioPermission extends BasicPermission{
    public AudioPermission(String name){
        super(name);
    }

    public AudioPermission(String name,String actions){
        super(name,actions);
    }
}
