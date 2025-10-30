package com.example.dndcharacterviewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.dndcharacterviewer.databinding.FragmentFirstBinding
import org.json.JSONObject


class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    // === helpers at CLASS level ===
    private fun fmtMod(n: Int): String = if (n >= 0) "+$n" else "$n"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load sample DMV JSON from assets, hardcoded for now
        val json = requireContext().assets.open("cinder.json").bufferedReader().use { it.readText() }
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

        //Abilities bonuses values
        val abilitiesBonus = abilitiesBlock?.optJSONObject("bonuses")
        val strBonus = abilitiesBonus?.optInt("str") ?: 0
        val dexBonus = abilitiesBonus?.optInt("dex") ?: 0
        val conBonus = abilitiesBonus?.optInt("con") ?: 0
        val intBonus = abilitiesBonus?.optInt("int") ?: 0
        val wisBonus = abilitiesBonus?.optInt("wis") ?: 0
        val chaBonus = abilitiesBonus?.optInt("cha") ?: 0

        //Saving throws values
        val saveBonuses = character.optJSONObject("save_bonuses")
        val strSave = saveBonuses?.optInt("str")
        val dexSave = saveBonuses?.optInt("dex")
        val conSave = saveBonuses?.optInt("con")
        val intSave = saveBonuses?.optInt("int")
        val wisSave = saveBonuses?.optInt("wis")
        val chaSave = saveBonuses?.optInt("cha")

        // Class/Level
        // TODO: Needs fixing - does not work with other JSON files
        val classes = character.optJSONObject("classes")?.optJSONObject("blood-hunter")
        val className = classes?.optString("class-name", "Class") ?: "Class"
        val subclass = classes?.optString("subclass-name", "none")
        val level = classes?.optInt("class-level", 0) ?: 0
        val classLine = buildString {
            append(className)
            if (!subclass.isNullOrBlank()) append(" (").append(subclass.replace('-', ' ')).append(")")
            append(" — L").append(level)
        }

        //Senses section
        val passivePerception = character?.optInt("passive_perception")
        val passiveInvestigation = 18 //hardcoded for now
        val passiveInsight = 15 //hardcoded for now



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
        // Display both value and modifier together,"16 (+3)"
        binding.strVal.text = "${str} (${fmtMod(strBonus)})"
        binding.dexVal.text = "${dex} (${fmtMod(dexBonus)})"
        binding.conVal.text = "${con} (${fmtMod(conBonus)})"
        binding.intVal.text = "${int_} (${fmtMod(intBonus)})"
        binding.wisVal.text = "${wis} (${fmtMod(wisBonus)})"
        binding.chaVal.text = "${cha} (${fmtMod(chaBonus)})"

        //Saving throws binding
        binding.strSaveVal.text = "$strSave"
        binding.dexSaveVal.text = "$dexSave"
        binding.conSaveVal.text = "$conSave"
        binding.intSaveVal.text = "$intSave"
        binding.wisSaveVal.text = "$wisSave"
        binding.chaSaveVal.text = "$chaSave"

        //Senses binding
        binding.ppVal.text = "$passivePerception"
        binding.piVal.text = "$passiveInvestigation"
        binding.psiVal.text = "$passiveInsight"






    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
