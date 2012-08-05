package com.dmtprogramming.pathfindercombat;

import java.util.Iterator;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.dmtprogramming.pathfindercombat.database.DatabaseHelper;
import com.dmtprogramming.pathfindercombat.models.*;
import com.dmtprogramming.pathfindercombat.modifier.ModifierBase;
import com.dmtprogramming.pathfindercombat.modifier.ModifierBase.ModifierField;
import com.dmtprogramming.pathfindercombat.WeaponActivity;

public class CharacterCombatFragment extends FragmentBase {

	private static final String TAG = "PFCombat:CharacterCombatFragment";
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		_view = inflater.inflate(R.layout.character_combat_fragment, container, false);
		
		setupIntentFilter();
		
        setupView();
        populateStats("");

        setupTriggers();
        
        setupButtons();
        
		return _view;
	}
	
    private void setupTriggers() {
    	setupEditTextTrigger(R.id.txtDailyTitle, DatabaseHelper._c_characters_daily_title);
    	setupEditTextTrigger(R.id.txtDailyCurrent, DatabaseHelper._c_characters_daily_current);
    	setupEditTextTrigger(R.id.txtDailyTotal, DatabaseHelper._c_characters_daily_total);
    	
    	//setupSpinnerTrigger(R.id.spinWeaponPlus, DatabaseHelper._c_characters_weapon_plus);
    	//setupSpinnerTrigger(R.id.spinDamage, DatabaseHelper._c_characters_weapon_damage);
    	//setupSpinnerTrigger(R.id.spinCriticalMultiplier, DatabaseHelper._c_characters_critical_multiplier);
	}
    
    private void setupButtons() {
    	Button newWeapon = (Button) findViewById(R.id.btnWeapons);
    	newWeapon.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent myIntent = new Intent(view.getContext(), WeaponActivity.class);;

				myIntent.putExtra("CHARACTER_ID", getCharacter().getId());
				startActivityForResult(myIntent, 0);	            
			}
		});
    }
    	

	protected void populateStats(String f) {
    	Log.d(TAG, "populateStats(" + f + ")");
    	
    	// save some typing
    	PFCharacter c = getCharacter();
    	Weapon w = c.getWeapon();
		
    	populateField(R.id.txtWeaponDice, f, ModifierField._damage_dice, w.getDamageDice());
    	
    	populateField(R.id.txtAttacks, f, ModifierField._none, c.getAttacks());
    	populateField(R.id.txtStrModPlusAttack, f, ModifierField._str_mod, String.valueOf(c.getStrMod()));
    	populateField(R.id.txtWeaponPlusAttack, f, ModifierField._none, String.valueOf(w.getHit()));
    	populateField(R.id.txtWeaponFocusPlusAttack, f, ModifierField._none, String.valueOf(c.getWeaponFocusMod()));
    	populateField(R.id.txtOtherPlusAttack, f, ModifierField._hit, String.valueOf(0));
    	
    	populateField(R.id.txtStrModPlusDamage, f, ModifierField._str_mod, String.valueOf(c.getStrMod()));
    	populateField(R.id.txtWeaponPlusDamage, f, ModifierField._none, String.valueOf(w.getDamage()));
    	populateField(R.id.txtOtherPlusDamage, f, ModifierField._damage, String.valueOf(c.getPowerAttackDamage()));
    	
    	populateField(R.id.txtDailyCurrent, f, ModifierField._none, String.valueOf(c.getDailyCurrent()));
    	populateField(R.id.txtDailyTotal, f, ModifierField._none, String.valueOf(c.getDailyTotal()));
    	populateField(R.id.txtDailyTitle, f, ModifierField._none, c.getDailyTitle());
    	
    	// size modifier calcs
    	String weaponDice = "1d6"; //c.getWeaponDamage();
    	int sizeMod = 0;
    	String sizeStr = applyToggles(ModifierField._size, "");
    	if (!sizeStr.equals("")) {
    		sizeMod = Integer.parseInt(sizeStr);
    	}
    	if (sizeMod > 0) {
    		weaponDice = c.getEnlargedWeaponDamage();
    	} else if (sizeMod < 0) {
    		weaponDice = c.getReducedWeaponDamage();
    	}
    	TextView weaponDiceText = (TextView) findViewById(R.id.txtWeaponDice);
    	weaponDiceText.setText(weaponDice);

    	// attacks
    	TextView tvAttacks = (TextView) findViewById(R.id.txtAttacks);
		String attacks_str = applyToggles(ModifierField._attacks, c.getAttacks());
    	String extraAttack = applyToggles(ModifierField._extra_attack, "0");
    	if (!extraAttack.equals("0")) {
    		attacks_str = attacks_str.concat(" / " + getCharacter().getBAB());
    	}
		tvAttacks.clearFocus();
		tvAttacks.setText(attacks_str);
    	
		// weapon plus
    	/*Spinner spinWeaponPlus = (Spinner) findViewById(R.id.spinWeaponPlus);
    	TextView weaponPlusAttack = (TextView) findViewById(R.id.txtWeaponPlusAttack);
    	TextView weaponPlusDamage = (TextView) findViewById(R.id.txtWeaponPlusDamage);
    	weaponPlusAttack.clearFocus();
    	weaponPlusAttack.setText(spinWeaponPlus.getSelectedItem().toString());
    	weaponPlusDamage.clearFocus();
    	weaponPlusDamage.setText(spinWeaponPlus.getSelectedItem().toString());*/
    	
    	// plus to hit
    	int plusHit = 0;
    	plusHit += parseIntField(R.id.txtStrModPlusAttack);
    	//plusHit += parseIntField(R.id.spinWeaponPlus);
    	plusHit += parseIntField(R.id.txtWeaponFocusPlusAttack);
    	plusHit += parseIntField(R.id.txtOtherPlusAttack);
    	String attacks = (String) ((TextView) findViewById(R.id.txtAttacks)).getText();
    	calculateFinalPlusHit(attacks, plusHit);
    	
    	// plus to damage
    	int plusDamage = 0;
    	plusDamage += parseIntField(R.id.txtStrModPlusDamage);
    	//plusDamage += parseIntField(R.id.spinWeaponPlus);
    	plusDamage += parseIntField(R.id.txtOtherPlusDamage);  
    	calculateFinalPlusDamage(weaponDice, plusDamage);
	}

	private void setupView() {
    	//String[] weaponPlus = { "0", "1", "2", "3", "4", "5", "6", "7", "8" };
    	//populateSpinner(R.id.spinDamage, getCharacter().getWeaponDamage(), PFCharacter.MEDIUM_WEAPON_DAMAGES);
    	//populateSpinner(R.id.spinWeaponPlus, String.valueOf(getCharacter().getWeaponPlus()), weaponPlus);
    	//populateSpinner(R.id.spinCriticalMultiplier, getCharacter().getCriticalMultiplier(), PFCharacter.CRITICAL_MULIPLIERS);
    	
    	updateToggleTable();
	}
	
	private void updateToggleTable() {
    	TableLayout table = (TableLayout) findViewById(R.id.combat_toggles);
    	table.removeAllViews();
    	for (int i = 0; i < _mods.size(); i++) {
    		ModifierBase mod = _mods.get(i);
    		mod.setEnabled(false);
    	}
    	_mods.clear();
    	TableRow row = new TableRow(getActivity());
    	int position = 0;
    	
    	PFCombatApplication app = (PFCombatApplication)getActivity().getApplication();
    	List<ModifierBase> toggles = app.getToggles();
    	Iterator<ModifierBase> iter = toggles.iterator();
    	while (iter.hasNext()) {
    		ModifierBase mod = iter.next();

    		if (mod.appliesToCharacterClass(getCharacter().getCharacterClass()) &&
    				mod.appliesToRange("melee")) {
	    		
	    		ToggleButton button = new ToggleButton(getActivity());
	    		button.setTextOn(mod.name());
	    		button.setTextOff(mod.name());
	    		button.setText(mod.name());
	    		row.addView(button);
	    		
	        	button.setOnClickListener(new ToggleClickListener(button, mod, this));
	        	_mods.add(mod);
	    		
	    		position += 1;
	    		if (position == 4) {
	    			table.addView(row);
	    			row = new TableRow(getActivity());
	    			position = 0;
	    		}
	    		Log.d(TAG, "added modifier toggle name = " + mod.name());
    		}
    	}
    	if (position != 4) {
    		table.addView(row);
    	}
	}
	
    private void calculateFinalPlusHit(String attacks_str, int plusHit) {
    	TextView tv = (TextView) findViewById(R.id.txtPlusAttack);
    	String finalPlusAttack = "";
    	String[] attacks = attacks_str.split("/");
    	for (int i = 0; i < attacks.length; i++) {
    		int hit = Integer.parseInt(attacks[i].trim());
    		if (!finalPlusAttack.equals("")) {
    			finalPlusAttack += " / ";
    		}
    		finalPlusAttack = finalPlusAttack + String.valueOf(hit + plusHit);
    	}
    	tv.clearFocus();
    	tv.setText(finalPlusAttack);
    }
    
    private void calculateFinalPlusDamage(String weaponDice, int plusDamage) {
    	TextView tv = (TextView) findViewById(R.id.txtPlusDamage);
    	String plusDamageDice = applyToggles(ModifierField._damage_dice, "");
    	if (!plusDamageDice.equals("")) {
    		plusDamageDice = " + " + plusDamageDice;
    	}
    	weaponDice = applyToggles(ModifierField._critical_damage_dice, weaponDice);
    	String plusDamageStr = applyToggles(ModifierField._critical_damage, String.valueOf(plusDamage));
    	tv.clearFocus();
    	tv.setText(weaponDice + " + " + plusDamageStr + plusDamageDice);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
  
	@Override
	public void onAfterUpdateCharacter(String field) {
		if (field.equals(DatabaseHelper._c_characters_character_class)) {
			updateToggleTable();
		}
		populateStats(field);
	}
    
    @Override
	public void onResume() {
		super.onResume();
	}
}
