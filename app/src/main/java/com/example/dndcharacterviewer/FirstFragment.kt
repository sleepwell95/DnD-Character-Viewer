package com.example.dndcharacterviewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.dndcharacterviewer.databinding.FragmentFirstBinding
import org.json.JSONObject
import kotlin.math.floor

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    // === helpers at CLASS level (private is valid here) ===
    private fun fmtMod(n: Int): String = if (n >= 0) "+$n" else "$n"
    private fun abilityMod(score: Int): Int = floor((score - 10) / 2.0).toInt()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load sample DMV JSON from assets
        val json = requireContext().assets.open("sample/cinder.json").bufferedReader().use { it.readText() }
        val root = JSONObject(json)

        // DMV structure: character is an array; we take the first character
        val character = root.getJSONArray("character").getJSONObject(0)

        val name = character.optString("character_name", "Unknown")
        val ac = character.optInt("ac", 0)
        val speed = character.optJSONObject("characteristics")?.optInt("speed") // not present this way
            ?: character.optJSONArray("characteristics")?.optJSONObject(0)?.optInt("speed") ?: 0
        val initiative = character.optInt("initiative_bonus", 0)
        val initiativemax = initiative + 3

        // HP is an array of objects
        val hpArr = character.optJSONArray("hp")
        val hpMax = if (hpArr != null && hpArr.length() > 0) {
            hpArr.getJSONObject(0).optInt("hp_max", 0)
        } else 0

        // Abilities & bonuses
        val abilitiesBlock = character.optJSONArray("abilities_bonuses")?.optJSONObject(0)
        val abilities = abilitiesBlock?.optJSONObject("abilities")
        val str = abilities?.optInt("str") ?: 0
        val dex = abilities?.optInt("dex") ?: 0
        val con = abilities?.optInt("con") ?: 0
        val int_ = abilities?.optInt("int") ?: 0
        val wis = abilities?.optInt("wis") ?: 0
        val cha = abilities?.optInt("cha") ?: 0

        //Abilities bonuses stats
        val abilitiesBonus = abilitiesBlock?.optJSONObject("bonuses")
        val str_bonus = abilitiesBonus?.optInt("str") ?: 0
        val dex_bonus = abilitiesBonus?.optInt("dex") ?: 0
        val con_bonus = abilitiesBonus?.optInt("con") ?: 0
        val int_bonus = abilitiesBonus?.optInt("int") ?: 0
        val wis_bonus = abilitiesBonus?.optInt("wis") ?: 0
        val cha_bonus = abilitiesBonus?.optInt("cha") ?: 0
        // Class/Level
        val classes = character.optJSONObject("classes")?.optJSONObject("blood-hunter")
        val className = classes?.optString("class-name", "Class") ?: "Class"
        val subclass = classes?.optString("subclass-name", null)
        val level = classes?.optInt("class-level", 0) ?: 0
        val classLine = buildString {
            append(className)
            if (!subclass.isNullOrBlank()) append(" (").append(subclass.replace('-', ' ')).append(")")
            append(" — L").append(level)
        }


        // Bind to views
        //Basic vals binding
        binding.charName.text = name
        binding.charClass.text = classLine
        binding.acVal.text = "AC: $ac"
        binding.hpVal.text = "HP: $hpMax"
        binding.speedVal.text = "Speed: $speed"
        binding.initiativeVal.text = "Init: $initiativemax"
        binding.abilitiesLineHeader.text = "Abilities"
        //Abilities ,bonuses binding
//        binding.abilitiesLine.text = "STR $str  DEX $dex  CON $con  INT $int_  WIS $wis  CHA $cha"

// Display both value and modifier together, e.g. "16 (+3)"
        binding.strVal.text = "${str} (${fmtMod(str_bonus)})"
        binding.dexVal.text = "${dex} (${fmtMod(dex_bonus)})"
        binding.conVal.text = "${con} (${fmtMod(con_bonus)})"
        binding.intVal.text = "${int_} (${fmtMod(int_bonus)})"
        binding.wisVal.text = "${wis} (${fmtMod(wis_bonus)})"
        binding.chaVal.text = "${cha} (${fmtMod(cha_bonus)})"



    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
