package com.example.tenant.ui.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.tenant.R
import com.example.tenant.data.model.*
import com.example.tenant.ui.viewmodel.ChosenActivityViewModel
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import java.io.FileInputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

class ArendaObjectFragment : Fragment() {
    private lateinit var addContractButton: ExtendedFloatingActionButton
    private lateinit var nameTextView: TextView
    private lateinit var phoneNumberTextView: TextView
    private lateinit var dateOfConclusionTextView: TextView
    private lateinit var dateOfEndConclusionTextView: TextView
    private lateinit var sumTextView: TextView
    private lateinit var payTimeTextView: TextView
    private lateinit var zalogTextView: TextView
    private lateinit var viewModel: ChosenActivityViewModel
    private lateinit var imageView: ImageView

    private var contractWithTenant: ContractWithTenant? = null

    private var historyPayList: List<HistoryPay>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_arenda_object, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addContractButton = view.findViewById(R.id.addContractButton)
        initViews(view)

        addContractButton.setOnClickListener {
            val intent = Intent(activity, NewContractActivity::class.java)
            (activity as ChosenObjectActivity).objectAndCategory?.let {
                intent.putExtra("object", it)
                intent.putExtra("con_with", contractWithTenant)
            }
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel = (activity as ChosenObjectActivity).chosenActivityViewModel
        observeContractWithTenant()
        (activity as ChosenObjectActivity).objectAndCategory?.id?.let {
            viewModel.getContractWithTenantByObjectId(
                it
            )
        }

        setContractStatusChangeListener()
        observeHistoryPay()
//        (activity as ChosenObjectActivity).objectAndCategory?.id?.let {
//            contractWithTenant?.id?.let {cId ->
//                viewModel.getHistoryPay(it, cId)
//            }
//        }

        setContractPayListener()
    }

    private fun initViews(view: View){
        nameTextView = view.findViewById(R.id.nameInputTextView)
        phoneNumberTextView = view.findViewById(R.id.phoneInputTypeInputTextView)
        dateOfConclusionTextView = view.findViewById(R.id.dateOfConclusionInputTextView)
        dateOfEndConclusionTextView = view.findViewById(R.id.dateOfEndConclusionInputTextView)
        sumTextView = view.findViewById(R.id.sumInputTextView)
        payTimeTextView = view.findViewById(R.id.payTimeInputTextView)
        zalogTextView = view.findViewById(R.id.zalogInputTextView)
        imageView = view.findViewById(R.id.image)
    }

    private fun observeContractWithTenant(){
        viewModel.contractWithTenant.observe(viewLifecycleOwner, Observer {con->
            con?.let {list ->
                if(list.isNotEmpty()){
                    list[list.size - 1].let {
                        if(it.status == ContractStatus.ACTIVE){
                            contractWithTenant = it
                            nameTextView.text = "${it.firstName} ${it.lastName}"
                            phoneNumberTextView.text = it.phoneNumber
                            dateOfConclusionTextView.text = reformat(it.dateOfContract)
                            dateOfEndConclusionTextView.text = reformat(it.dateOfEnd)
                            sumTextView.text = it.sum.toString()
                            payTimeTextView.text = payTimeReformat(it.timeOfPay)
                            zalogTextView.text = (it.zalog ?: "нет").toString()
                            addContractButton.setIconResource(R.drawable.ic_edit)
                            addContractButton.text = "Редактировать"

                            (activity as ChosenObjectActivity).objectAndCategory?.let { obj->
                                if(obj.image != null) {
                                    Log.i("imggg", "yea")
                                    val file = getImage(obj.image)
                                    if(file != null){
                                        Log.i("imggg", "yeap")
                                        //Picasso.get().load(file).into(imageView)
                                        Glide.with(requireContext()).load(file).into(imageView)
                                    }
                                }
                            }
                            (activity as ChosenObjectActivity).objectAndCategory?.id?.let {obj_id ->
                                viewModel.getHistoryPay(obj_id, it.id)
                            }
                        }
                    }
                }
            }
        })
    }


    private fun reformat(date: Date): String{
        val format = "dd.MM.yyyy"
        val simpleDateFormat = SimpleDateFormat(format, Locale.ENGLISH)
        return simpleDateFormat.format(date)
    }

    private fun payTimeReformat(payTime: PayTime): String{
        return when(payTime){
            PayTime.DAY -> "Каждый день"
            PayTime.MONTH -> "Раз в месяц"
            PayTime.HALF_MONTH -> "Два раза в месяц"
        }
    }

    private fun setContractStatusChangeListener(){
        (activity as ChosenObjectActivity).setOnContractStatusChange(object : ChosenObjectActivity.OnContractStatusChange{
            override fun onStatusChange() {
                contractWithTenant?.let {
                    val contract = Contract(
                        it.id,
                        it.objectId,
                        it.tenantId,
                        it.sum,
                        it.dateOfContract,
                        it.dateOfEnd,
                        it.timeOfPay,
                        it.zalog,
                        ContractStatus.NO_ACTIVE
                    )

                    viewModel.upsertContract(contract)
                }
            }
        })
    }

    private fun setContractPayListener(){
        (activity as ChosenObjectActivity).setOnPayContractListener(object : ChosenObjectActivity.OnPayContractListener{
            override fun onPayContract() {
                historyPayList?.let{historyPays ->
                    if(contractWithTenant?.status == ContractStatus.ACTIVE){
                        val currentCalendar = Calendar.getInstance()
                        val dateConclusionCalendar = Calendar.getInstance()
                        dateConclusionCalendar.time = contractWithTenant?.dateOfContract!!

                        val cur = reformat(currentCalendar.time)
                        val conc = reformat(dateConclusionCalendar.time)
                        val dateFormatInput = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                        val curParse = LocalDate.parse(cur, dateFormatInput)
                        val concParse = LocalDate.parse(conc, dateFormatInput)


                        val historyPay = HistoryPay(
                            0,
                            (activity as ChosenObjectActivity).objectAndCategory!!.id,
                            contractWithTenant!!.sum,
                            contractWithTenant!!.id,
                            currentCalendar.time
                        )

                        if(contractWithTenant?.timeOfPay == PayTime.DAY){
                            val days = ChronoUnit.DAYS.between(concParse, curParse)
                            if(historyPays.size < days){
                                viewModel.addHistoryPay(historyPay)
                            }else{
                                Toast.makeText(requireContext(), "За сегодня уже оплачено", Toast.LENGTH_LONG).show()
                            }
                        }

                        if(contractWithTenant?.timeOfPay == PayTime.MONTH){
                            val months = ChronoUnit.MONTHS.between(concParse, curParse)
                            if(historyPays.size < months){
                                viewModel.addHistoryPay(historyPay)
                            }else{
                                Toast.makeText(requireContext(), "За этот месяц уже оплачено", Toast.LENGTH_LONG).show()
                            }
                        }
                    }

                    return
                }
                if(contractWithTenant?.status == ContractStatus.ACTIVE){
                    val currentCalendar = Calendar.getInstance()
                    Log.i("click pay", "yes")
                    val historyPay = HistoryPay(
                        0,
                        (activity as ChosenObjectActivity).objectAndCategory!!.id,
                        contractWithTenant!!.sum,
                        contractWithTenant!!.id,
                        currentCalendar.time
                    )

                    viewModel.addHistoryPay(historyPay)
                    Toast.makeText(requireContext(), "Оплата закрыта", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun observeHistoryPay(){
        viewModel.historyPay.observe(viewLifecycleOwner, Observer {
            historyPayList = it
        })
    }

    private fun getImage(name: String): ByteArray? {
        var fileInputStream: FileInputStream? = null
        try {
            fileInputStream = requireContext().openFileInput(name)
            val b = fileInputStream?.readAllBytes()
            Log.i("imggg", b.toString())
            //val file = File("img")

            //val fileOutputStream = FileOutputStream(file)
            //fileOutputStream.write(b)
            return b

        }catch (ex: IOException){
            ex.printStackTrace()
        }
        finally {
            fileInputStream?.close()
        }

        return null
    }
}