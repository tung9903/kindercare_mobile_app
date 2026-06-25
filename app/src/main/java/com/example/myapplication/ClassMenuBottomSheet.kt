package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import com.example.myapplication.View.GiaoVien.ManHinhChucNangBaoCaoSuCoYTe
import com.example.myapplication.View.GiaoVien.ManHinhChucNangSucKhoeVaChieuCao
import com.example.myapplication.View.GiaoVien.ManHinhDiemDanh
import com.example.myapplication.View.GiaoVien.ManHinhChatGiaoVien
import com.example.myapplication.Model.DataManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ClassMenuBottomSheet : BottomSheetDialogFragment() {

    override fun getTheme(): Int = R.style.CustomBottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_class_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val studentId = arguments?.getInt(ARG_STUDENT_ID, -1) ?: -1
        val student = DataManager.studentList.find { it.StudentID == studentId }

        if (student == null) {
            dismiss()
            return
        }

        // Ánh xạ các nút từ giao diện XML
        val btnChatParent = view.findViewById<LinearLayout>(R.id.btn_chat_parent)
        val btnAddNote = view.findViewById<LinearLayout>(R.id.btn_add_note)
        val btnUpdateHealth = view.findViewById<LinearLayout>(R.id.btn_update_health)
        val btnUpdateAbsent = view.findViewById<LinearLayout>(R.id.btn_update_absent)
        val btnGiveReward = view.findViewById<LinearLayout>(R.id.btn_give_reward)
        val btnReportIncident = view.findViewById<LinearLayout>(R.id.btn_report_incident)

        // Điều kiện hiển thị nút Báo cáo sự cố y tế
        if (student.Allergies.isNullOrEmpty() || student.Allergies == "Không") {
            btnReportIncident.visibility = View.GONE
        } else {
            btnReportIncident.visibility = View.VISIBLE
        }

        // Bắt sự kiện click
        btnChatParent.setOnClickListener {
            val intent = Intent(context, ManHinhChatGiaoVien::class.java)
            intent.putExtra("PARENT_NAME", student.parentName)
            intent.putExtra("STUDENT_NAME", student.FullName)
            startActivity(intent)
            dismiss()
        }

        btnAddNote.setOnClickListener {
            handleAction("Thêm ghi chú nội bộ")
        }

        btnUpdateHealth.setOnClickListener {
            val intent = Intent(context, ManHinhChucNangSucKhoeVaChieuCao::class.java)
            intent.putExtra("STUDENT_ID", studentId)
            startActivity(intent)
            dismiss()
        }

        btnUpdateAbsent.setOnClickListener {
            val intent = Intent(context, ManHinhDiemDanh::class.java)
            startActivity(intent)
            dismiss()
        }

        btnGiveReward.setOnClickListener {
            handleAction("Tặng phiếu bé ngoan")
        }

        btnReportIncident.setOnClickListener {
            val currentContext = context
            if (currentContext != null) {
                val intent = Intent(currentContext, ManHinhChucNangBaoCaoSuCoYTe::class.java)
                intent.putExtra("TEN_HOC_SINH", student.FullName)
                intent.putExtra("NOI_DUNG_DI_UNG", student.Allergies)
                startActivity(intent)
            }
            dismiss()
        }
    }

    private fun handleAction(actionName: String) {
        Toast.makeText(context, "Bạn vừa chọn: $actionName", Toast.LENGTH_SHORT).show()
        dismiss()
    }

    companion object {
        private const val ARG_STUDENT_ID = "student_id"

        fun newInstance(studentId: Int): ClassMenuBottomSheet {
            val fragment = ClassMenuBottomSheet()
            val args = Bundle()
            args.putInt(ARG_STUDENT_ID, studentId)
            fragment.arguments = args
            return fragment
        }
    }
}
