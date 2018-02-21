package cl.datageneral.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;
import android.provider.SyncStateContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cl.datageneral.findit.Constants;
import cl.datageneral.findit.Posicion;

/**
 * Created by Pablo Molina C. on 31-08-2017.
 */

public final class Query extends DatabaseManager{

    /**
     * Recupero la Instancia del objeto SQLiteDatabase
     * @return
     */
    private static SQLiteDatabase a(){
        return DatabaseManager.getInstance().openDatabase();
    }

    private static Cursor getCursor(String sql, String[] args ){
        return a().rawQuery(sql, args);
    }

    private static HashMap<Integer,HashMap<String, String>> getMap(String sql, String[] args) {
        Cursor _data = a().rawQuery(sql, args);
        HashMap<Integer,HashMap<String, String>> r = CursorHashMap(_data);
        _data.close();
        return r;
    }

    private static HashMap<Integer,HashMap<String, String>> CursorHashMap(Cursor _data){
        int cont = 0;
        HashMap<Integer,HashMap<String, String>> result = new HashMap<>(_data.getCount());
        if (_data.moveToFirst()){
            do {
                HashMap<String, String> row = new HashMap<>(_data.getColumnCount());
                String[] ite = _data.getColumnNames();
                for(String h: ite){
                    row.put(h, _data.getString(_data.getColumnIndex(h)) );
                }
                result.put(cont, row);
                cont++;
            } while (_data.moveToNext());
        }
        _data.close();
        return result;
    }

    /**
     * TODAS LAS QUERYS A SQLITE VAN ACA
     */
    public static void insDevice(String rut, String dispositivo, String nombre) {
        String[] args={dispositivo, rut, dispositivo, nombre};
        String sql =
                "INSERT OR REPLACE INTO devices (_id, rut, dispositivo, nombre, color, estado) " +
                "VALUES((SELECT _id FROM devices WHERE dispositivo=?), ?, ?, ?,( " +
                        "SELECT _id FROM ( " +
                        "SELECT colors._id, colors.name, colors.value, COUNT(devices.color) cnt " +
                        "FROM colors " +
                        "LEFT JOIN devices ON devices.color = colors._id " +
                        "GROUP BY devices.color " +
                        "ORDER BY devices._id asc) ORDER BY cnt asc LIMIT 1),'P')";
        a().execSQL(sql, args);
    }
    public static long insSimple(String table, ContentValues newValues) {
        return a().insert(table, null, newValues);
    }
    public static long insertOrThrow(String table, ContentValues newValues) {
        return a().insertOrThrow(table, null, newValues);
    }
    public static boolean updDevice(String _rowIndex, String idtoken) {
        String[] args={_rowIndex};
        ContentValues newValues = new ContentValues();
        newValues.put("estado", "N");
        newValues.put("idt", idtoken);
        return a().update("devices", newValues, "dispositivo = ?", args) > 0;
    }
    public static HashMap<Integer,HashMap<String, String>> selectDevice(String device) {
        String[] args={device};
        return getMap("SELECT devices._id, dispositivo, nombre, colors.name, colors.value " +
                "FROM devices " +
                "LEFT JOIN colors ON colors._id = devices.color " +
                "WHERE dispositivo = ?", args);
    }
    public static HashMap<String, String> selectDevice() {
        HashMap<String, String> r = new HashMap<>(10);
        Cursor _data = a().rawQuery("SELECT devices._id, dispositivo, nombre, colors.name, colors.value, idt " +
                "FROM devices " +
                "LEFT JOIN colors ON colors._id = devices.color " +
                "WHERE estado='N' ORDER BY devices._id DESC", null);
        if (_data.moveToFirst()){
            String[] ite = _data.getColumnNames();
            for(String h: ite){
                r.put(h, _data.getString(_data.getColumnIndex(h)) );
            }
        }
        return r;
    }
    public static HashMap<Integer,HashMap<String, String>> selectDevices() {
        return getMap("SELECT devices._id, dispositivo, nombre, colors.name, colors.value, idt " +
                "FROM devices " +
                "LEFT JOIN colors ON colors._id = devices.color " +
                "WHERE estado='N'", null);
    }
    public static HashMap<Integer,HashMap<String, String>> getPosicionByID(String id) {
        String[] args={id};
        return getMap("SELECT devices.dispositivo, devices.nombre, colors.value, posiciones.latitude, " +
                "posiciones.longitude, posiciones.battery, posiciones.distance, posiciones.server_time " +
                "FROM devices " +
                "LEFT JOIN posiciones ON posiciones.dispositivo=devices.dispositivo " +
                "LEFT JOIN colors ON colors._id = devices.color " +
                "WHERE posiciones._id = ? " +
                "ORDER BY posiciones._id DESC LIMIT 1", args);
    }
    public static HashMap<Integer,HashMap<String, String>> getPosicionDevice(String device) {
        String[] args={device};
        return getMap("SELECT devices.dispositivo, devices.nombre, colors.value, posiciones.latitude, " +
                "posiciones.longitude, posiciones.battery, posiciones.distance, posiciones.server_time " +
                "FROM devices " +
                "LEFT JOIN posiciones ON posiciones.dispositivo=devices.dispositivo " +
                "LEFT JOIN colors ON colors._id = devices.color " +
                "WHERE devices.dispositivo = ? " +
                "ORDER BY posiciones._id DESC LIMIT 1", args);
    }
    public static HashMap<Integer,HashMap<String, String>> getTracertDevice(String device, String job) {
        String[] args={device, job};
        return getMap("SELECT posiciones._id, devices.dispositivo, devices.nombre, colors.value, posiciones.latitude, " +
                "posiciones.longitude, posiciones.battery, posiciones.distance, posiciones.server_time " +
                "FROM devices " +
                "LEFT JOIN posiciones ON posiciones.dispositivo=devices.dispositivo " +
                "LEFT JOIN colors ON colors._id = devices.color " +
                "WHERE devices.dispositivo = ? AND job = ? " +
                "ORDER BY posiciones._id ASC", args);
    }
    public static HashMap<Integer,HashMap<String, String>> getNotificaciones() {
        return getMap("SELECT _id, leida, fecha, titulo, mensaje, tipo " +
                "from notificaciones " +
                "WHERE titulo IS NOT NULL AND tipo IS NOT NULL AND fecha IS NOT NULL " +
                "ORDER BY _id desc", null);
    }
    public static HashMap<Integer,HashMap<String, String>> getPagos() {
        return getMap("SELECT pagos._id, pagos.id_device, pagos.estado_pago, pagos.fecha_vencimiento, " +
                "   pagos.fecha_ultimo_pago, pagos.comentario, devices.nombre " +
                "FROM pagos " +
                "LEFT JOIN devices on devices.dispositivo = pagos.id_device " +
                "WHERE devices._id IS NOT NULL " +
                "ORDER BY devices._id", null);
    }
    public static boolean updNotificacion(String _rowIndex, String leida) {
        ContentValues newValues = new ContentValues();
        newValues.put("leida", leida);
        return a().update("notificaciones", newValues, "_id = " + _rowIndex, null) > 0;
    }
    public static void truncate(String TABLE_NAME) {
        a().execSQL("delete from "+ TABLE_NAME);
    }
    public static HashMap<Integer,HashMap<String, String>> selectAmount(String device) {
        String[] args={device};
        return getMap("SELECT _id, nombre, device FROM MONTOS WHERE device=? ORDER BY _id", args);
    }
    public static boolean updPosicion(String id, String job) {
        ContentValues newValues = new ContentValues();
        newValues.put("job", job);
        return a().update("posiciones", newValues, "_id = " + id, null) > 0;
    }
    public static void insFixPosition(Posicion jPosition, String job) {
        String[] args={job,
                jPosition.getId(),
                jPosition.getId(),
                jPosition.getDispositivo(),
                jPosition.getServer_time(),
                jPosition.getLatitude(),
                jPosition.getLongitude(),
                jPosition.getBattery(),
                jPosition.getDistance(),
                jPosition.getSpeed()};
        String sql =
                "UPDATE posiciones SET job = ? WHERE _id = ?; " +
                "INSERT OR REPLACE INTO posiciones(_id, dispositivo, server_time, latitude, longitude, battery, distance, speed, job) " +
                "VALUES (?,?,?,?,?,?,?,?,?) WHERE changes() = 0;";
        a().execSQL(sql, args);
    }



}
