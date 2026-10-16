package lk.susa.plant_lk.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import lk.susa.plant_lk.model.PlantCareTip;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "plant_lk.db";
    private static final int DATABASE_VERSION = 2;
    public static final String TABLE_TIPS = "plant_tips";
    public static final String COL_ID = "id";
    public static final String COL_TITLE = "title";
    public static final String COL_TREATMENTS = "treatments";
    public static final String COL_PESTS = "pests";
    public static final String COL_SIDE_EFFECTS = "side_effects";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_TIPS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TITLE + " TEXT UNIQUE, "
                + COL_TREATMENTS + " TEXT, "
                + COL_PESTS + " TEXT, "
                + COL_SIDE_EFFECTS + " TEXT)";
        db.execSQL(CREATE_TABLE);

        insertDefaultTips(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            //db.execSQL("ALTER TABLE " + TABLE_TIPS + " ADD COLUMN " + COL_TREATMENTS + " TEXT");
        }
    }


    private void insertDefaultTips(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            insertTipIfNotExists(db, "Watering",
                    "Water your plants 2-3 times a week depending on species. Check soil moisture before watering to avoid root rot. Use lukewarm water for sensitive plants and water in the morning to reduce evaporation.",
                    "Fungus gnats, root rot pests.",
                    "Overwatering damages roots.");

            insertTipIfNotExists(db, "Sunlight",
                    "Place your plants in indirect sunlight for optimal growth. Rotate plants weekly to ensure even growth. Adjust according to species: succulents prefer bright light, while ferns thrive in low light.",
                    "Leaf burn if too much sun exposure.",
                    "Too much heat dries plant or causes leaf yellowing.");

            insertTipIfNotExists(db, "Fertilizer",
                    "Use organic fertilizer once a month for healthy growth. For flowering plants, apply bloom-boosting fertilizer every 6 weeks. Avoid over-fertilizing as it can damage roots.",
                    "Excess fertilizer attracts pests like ants or aphids.",
                    "Over-fertilization may burn roots or stunt growth.");

            insertTipIfNotExists(db, "Pruning",
                    "Remove dead leaves and stems regularly to promote new growth. Trim overgrown branches to maintain shape and improve air circulation. Use clean, sharp tools to prevent infections.",
                    "Improper pruning can attract sap-feeding insects.",
                    "Cutting too much can stress the plant.");

            insertTipIfNotExists(db, "Pest Control",
                    "Check for pests like aphids, mites, and mealybugs regularly. Treat naturally with neem oil or insecticidal soap. Quarantine new plants to prevent infestations.",
                    "Aphids, mites, mealybugs, fungus gnats.",
                    "Overuse of pesticides may harm beneficial insects or the plant.");

            insertTipIfNotExists(db, "Soil Care",
                    "Use well-draining soil suited to your plant type. Mix compost or perlite for better aeration. Repot every 1-2 years to refresh soil nutrients.",
                    "Poor soil attracts insects and mold.",
                    "Bad soil affects growth and can cause root rot.");

            insertTipIfNotExists(db, "Humidity",
                    "Maintain 40-60% humidity for tropical plants. Use a humidifier or place a water tray nearby. Group plants together to naturally increase humidity.",
                    "Low humidity attracts spider mites.",
                    "Too high humidity may cause fungal growth or leaf damage.");

            insertTipIfNotExists(db, "Temperature",
                    "Keep indoor plants in a stable temperature range (18-24°C). Avoid placing them near heaters, air conditioners, or drafty windows.",
                    "Cold drafts or heat spikes may attract pests.",
                    "Extreme temperatures can stunt growth or cause leaf drop.");

            insertTipIfNotExists(db, "Seasonal Care",
                    "Adjust watering, sunlight, and fertilization according to season. Reduce watering in winter when growth slows. Increase light exposure during short winter days.",
                    "Pests may increase during certain seasons.",
                    "Incorrect seasonal care may weaken plants or delay growth.");

            insertTipIfNotExists(db, "Support & Training",
                    "Use stakes, trellises, or plant ties for climbing or heavy plants. Train vines gradually to prevent breakage and encourage proper growth direction.",
                    "Unstable supports can allow pests to hide.",
                    "Improper training may break branches or stems.");

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }


    private void insertTipIfNotExists(SQLiteDatabase db, String title, String treatments, String pests, String sideEffects) {
        Cursor cursor = db.rawQuery(
                "SELECT id FROM " + TABLE_TIPS + " WHERE LOWER(title) = LOWER(?)",
                new String[]{title.trim()}
        );

        boolean exists = cursor != null && cursor.moveToFirst();
        if (cursor != null) cursor.close();

        if (!exists) {
            ContentValues values = new ContentValues();
            values.put(COL_TITLE, title.trim());
            values.put(COL_TREATMENTS, treatments);
            values.put(COL_PESTS, pests);
            values.put(COL_SIDE_EFFECTS, sideEffects);
            db.insert(TABLE_TIPS, null, values);
        }
    }


    public List<PlantCareTip> getAllTips() {
        List<PlantCareTip> tips = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TIPS, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
                String treatments = cursor.getString(cursor.getColumnIndexOrThrow(COL_TREATMENTS));
                String pests = cursor.getString(cursor.getColumnIndexOrThrow(COL_PESTS));
                String sideEffects = cursor.getString(cursor.getColumnIndexOrThrow(COL_SIDE_EFFECTS));

                PlantCareTip tip = new PlantCareTip(id, title, "");
                tip.setTreatments(treatments);
                tip.setPests(pests);
                tip.setSideEffects(sideEffects);

                tips.add(tip);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return tips;
    }


    public PlantCareTip getTipByTitle(String title) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_TIPS + " WHERE LOWER(title) = LOWER(?)",
                new String[]{title.trim()}
        );

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
            String treatments = cursor.getString(cursor.getColumnIndexOrThrow(COL_TREATMENTS));
            String pests = cursor.getString(cursor.getColumnIndexOrThrow(COL_PESTS));
            String sideEffects = cursor.getString(cursor.getColumnIndexOrThrow(COL_SIDE_EFFECTS));
            cursor.close();

            PlantCareTip tip = new PlantCareTip(id, title, "");
            tip.setTreatments(treatments);
            tip.setPests(pests);
            tip.setSideEffects(sideEffects);

            return tip;
        }

        return null;
    }
}