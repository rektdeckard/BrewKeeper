package com.tobiasfried.brewkeeper.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.tobiasfried.brewkeeper.constants.*;

import static com.tobiasfried.brewkeeper.data.Ingredient.TABLE_NAME;

@Entity (tableName = TABLE_NAME)
public class Ingredient {

    public static final String TABLE_NAME = "ingredients";

    // MEMBER FIELDS

    // ID Column
    @PrimaryKey (autoGenerate = true)
    private int id;

    // Ingredient ID Column
    private int ingredientId;

    // Name Column
    private String name;

    // Ingredient Type Column
    private IngredientType type;

    // Tea Type Column
    private TeaType teaType;


    // CONSTRUCTORS
    /**
     * Constructor for programmatic use
     * @param name Ingredient name
     * @param type {@link IngredientType}
     * @param teaType {@link TeaType}
     */
    public Ingredient(String name, IngredientType type, TeaType teaType) {
        this.name = name;
        this.type = type;
        this.teaType = teaType;
    }

    /**
     * Constructor for Room use only
     * @param id Autoincremented
     * @param name Ingredient name
     * @param type {@link IngredientType}
     * @param teaType {@link TeaType}
     * @param ingredientID UniqueID
     */
    public Ingredient(int id, String name, IngredientType type, TeaType teaType, int ingredientID) {
        this.id = id;
        this.ingredientId = ingredientID;
        this.name = name;
        this.type = type;
        this.teaType = teaType;
    }


    // GETTERS

    public int getId() {
        return id;
    }

    public int getIngredientId() {
        return ingredientId;
    }

    public String getName() {
        return name;
    }

    public IngredientType getType() {
        return type;
    }

    public TeaType getTeaType() {
        return this.type == IngredientType.TEA ? teaType : null;
    }


    // SETTERS

    public void setId(int id) {
        this.id = id;
    }

    public void setIngredientId(int ingredientId) {
        this.ingredientId = ingredientId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(IngredientType type) {
        this.type = type;
    }

    public void setTeaType(TeaType teaType) {
        this.teaType = teaType;
    }

}