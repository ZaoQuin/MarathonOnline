package com.university.marathononline.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.university.marathononline.R
import com.university.marathononline.databinding.FragmentMonthlyStatisticsBinding
import com.university.marathononline.ui.components.MonthPickerBottomSheetFragment
import com.university.marathononline.ui.viewModel.MonthlyStatisticsViewModel
import com.university.marathononline.utils.DateUtils
import java.util.Calendar

class MonthlyStatisticsFragment : Fragment() {

    private lateinit var binding: FragmentMonthlyStatisticsBinding
    private val viewModel: MonthlyStatisticsViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMonthlyStatisticsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()
    }

    private fun initUI() {
        // Khởi tạo tháng và năm mặc định là tháng hiện tại
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) // Lấy tháng hiện tại
        val currentYear = Calendar.getInstance().get(Calendar.YEAR) // Lấy năm hiện tại

        // Cập nhật filterText hiển thị tháng/năm hiện tại
        binding.filterText.text = DateUtils.getFormattedMonthYear(currentMonth, currentYear)

        // Thiết lập sự kiện click cho nút filter để mở BottomSheet chọn tháng/năm
        binding.filterButton.setOnClickListener { showMonthPickerBottomSheet() }

        // Các thiết lập khác cho giao diện người dùng (ví dụ: LineChart, dữ liệu, v.v.)
        setUpLineChart()
    }

    lateinit var lineChart: LineChart

    private fun setUpLineChart() {
        val lineChart = binding.lineChart

        // Cấu hình trục X và Y
        val xAxis = lineChart.xAxis
        val leftAxis = lineChart.axisLeft
        val rightAxis = lineChart.axisRight

        // Cấu hình trục X
        xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setLabelCount(10, true) // Hiển thị tối đa 10 nhãn trên trục X
            textColor = ContextCompat.getColor(requireContext(), R.color.text_color)
            gridColor = ContextCompat.getColor(requireContext(), R.color.light_gray)
            axisLineColor = ContextCompat.getColor(requireContext(), R.color.dark_main_color)
            granularity = 1f
            isGranularityEnabled = true
        }

        // Cấu hình trục Y
        leftAxis.apply {
            textColor = ContextCompat.getColor(requireContext(), R.color.text_color)
            gridColor = ContextCompat.getColor(requireContext(), R.color.light_gray)
            axisLineColor = ContextCompat.getColor(requireContext(), R.color.dark_main_color)
        }

        rightAxis.isEnabled = false // Tắt trục Y bên phải

        // Cấu hình biểu đồ LineDataSet
        val entries = ArrayList<Entry>()
        val calendar = Calendar.getInstance()

        // Giả sử dữ liệu là cho 30 ngày (1 tháng)
        for (i in 0 until 30) {
            val day = i // Sử dụng i làm ngày trong tháng
            val value = getDataForDay(day) // Giả sử getDataForDay() là hàm lấy giá trị cho ngày đó, nếu không có thì trả về 0

            // Nếu không có dữ liệu cho ngày, giá trị là 0
            if (value == null) {
                entries.add(Entry(day.toFloat(), 0f))
            } else {
                entries.add(Entry(day.toFloat(), value.toFloat()))
            }
        }

        val dataSet = LineDataSet(entries, "Lượt chạy")
        dataSet.apply {
            color = ContextCompat.getColor(requireContext(), R.color.main_color) // Màu đường
            lineWidth = 2f
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.light_main_color)) // Màu các điểm
            circleRadius = 5f
            setDrawFilled(true) // Vẽ nền dưới đường
            fillColor = ContextCompat.getColor(requireContext(), R.color.light_main_color) // Màu nền dưới đường
            fillAlpha = 80 // Tạo độ mờ cho nền dưới đường, làm cho nó mềm mại hơn
            mode = LineDataSet.Mode.CUBIC_BEZIER // Đường cong mượt (có thể thay đổi để thử các chế độ khác)
        }

        // Dữ liệu cho LineChart
        val lineData = LineData(dataSet)
        lineChart.data = lineData

        // Cập nhật giao diện
        lineChart.apply {
            setDrawGridBackground(false) // Tắt lưới nền
            description.isEnabled = false // Tắt mô tả
            legend.apply {
                isEnabled = true
                textColor = ContextCompat.getColor(requireContext(), R.color.text_color) // Màu của legend
            }
            setTouchEnabled(true) // Cho phép tương tác với biểu đồ
            animateXY(1500, 1500) // Hiệu ứng mượt khi vẽ biểu đồ
            // Bật tính năng zoom để người dùng có thể zoom vào biểu đồ khi cần
            setPinchZoom(true)
            setScaleEnabled(true)
        }
    }


    private fun getDataForDay(day: Int): Int? {
        return if (day % 2 == 0)
            5
        else
            null
    }

    private fun showMonthPickerBottomSheet() {
        // Tạo và hiển thị MonthPickerBottomSheetFragment
        val bottomSheet = MonthPickerBottomSheetFragment { month, year ->
            // Cập nhật filterText và thực hiện các hành động cần thiết sau khi chọn tháng và năm
            binding.filterText.text = DateUtils.getFormattedMonthYear(month, year)
            viewModel.filterDataByMonth(month, year) // Gọi viewModel để lọc dữ liệu theo tháng/năm
        }
        bottomSheet.show(parentFragmentManager, bottomSheet.tag)
    }
}