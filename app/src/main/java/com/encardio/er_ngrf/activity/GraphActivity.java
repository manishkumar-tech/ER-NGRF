package com.encardio.er_ngrf.activity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.encardio.er_ngrf.tool.Constant;
import com.encardio.er_ngrf.tool.Tool;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class GraphActivity extends AppCompatActivity {

    static int flag = 0;
    static int flag2 = 0;
    String err_msg = "";

    EditText yaxis_range_min, yaxis_range_max;
    TextView dataRange_from, dataRange_to, xaxis_range_from, xaxis_range_to;
    ImageView minplus, secplus, hourplus, hourminus, minminus, secminus;
    Calendar myCalendar;
    TextView actualtime;
    TextView hourvalue;
    TextView minvalue;
    TextView secvalue;
    String data_setup;
    String range_date_from, range_date_to, range_time_to, range_time_from;
    DatePickerDialog.OnDateSetListener datePicker = new DatePickerDialog.OnDateSetListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            try {
                String DateForm = parseDate(myCalendar.getTime().toString());
                DateForm = DateForm.replace("-", "/");
                if (flag == 0) {
                    range_date_from = DateForm;
                    xaxis_range_from.setText(range_date_from + " " + range_time_from);
                } else if (flag == 1) {
                    range_date_to = DateForm;
                    xaxis_range_to.setText(range_date_to + " " + range_time_to);
                }
                data_setup = DateForm;
            } catch (Exception ignored) {
            }
        }
    };
    private GraphicalView mChart;
    private boolean exceptionSetup;
    private final Handler repeatUpdateHandler = new Handler();
    private boolean mAutoIncrement = false;
    private boolean mAutoDecrement = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_graph);

        yaxis_range_max = findViewById(R.id.max_yaxis_edtxt);
        yaxis_range_min = findViewById(R.id.min_yaxis_edtxt);
        myCalendar = Calendar.getInstance();
        setParameterDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @SuppressLint("SetTextI18n")
    public void setParameterDialog() {

        final Button ok = findViewById(R.id.para_ok);
        Button cancel = findViewById(R.id.para_cancel);
        final ImageView selectDateFrom = findViewById(R.id.btn_date_xrange_from);
        final ImageView selectTimeFrom = findViewById(R.id.btn_time_xrange_from);
        final ImageView selectDateTo = findViewById(R.id.btn_date_xrange_to);
        final ImageView selectTimeTo = findViewById(R.id.btn_time_xrange_to);
        xaxis_range_from = findViewById(R.id.from_xaxis_edtxt);
        xaxis_range_to = findViewById(R.id.to_xaxis_edtxt);
        dataRange_from = findViewById(R.id.from_edtxt);
        dataRange_to = findViewById(R.id.to_edtxt);

        dataRange_from.setText(Constant.graph_date_time[Constant.graph_length - 1]);
        range_date_from = dataRange_from.getText().toString().substring(0, 10);
        range_time_from = dataRange_from.getText().toString().substring(11, 19);
        dataRange_to.setText(Constant.graph_date_time[0]);
        range_date_to = dataRange_to.getText().toString().substring(0, 10);
        range_time_to = dataRange_to.getText().toString().substring(11, 19);

        yaxis_range_max.setText("" + Constant.graph_max);
        yaxis_range_min.setText("" + Constant.graph_min);

        xaxis_range_from.setText(Constant.graph_date_time[Constant.graph_length - 1]);
        xaxis_range_to.setText(Constant.graph_date_time[0]);

        ok.setOnClickListener(v -> {
            Tool.controlButtonDebouncing(ok);
            if ((yaxis_range_max.getText().toString()).equals(yaxis_range_min.getText().toString())) {
                Toast.makeText(GraphActivity.this, "Maximum and Minimum value cannot be equal", Toast.LENGTH_LONG).show();
            } else {
                float check_max = Float.parseFloat(yaxis_range_max.getText().toString());
                float check_min = Float.parseFloat(yaxis_range_min.getText().toString());
                if (check_max > check_min) {
                    Constant.graph_max = check_max;
                    Constant.graph_min = check_min;
                    if (validateFromDate(xaxis_range_from.getText().toString()) || validateToDate(xaxis_range_to.getText().toString())) {
                        if (exceptionSetup) {
                            exceptionSetup = false;
                            Toast.makeText(GraphActivity.this, err_msg, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        setContentView(R.layout.graph);
                        openChart();
                    }
                } else {
                    Toast.makeText(GraphActivity.this, "Maximum range must be greater than Minimum range", Toast.LENGTH_LONG).show();
                }
            }
        });

        cancel.setOnClickListener(v -> finish());

        selectDateFrom.setOnClickListener(v -> {
            flag = 0;
            new DatePickerDialog(GraphActivity.this, datePicker, Integer.parseInt(xaxis_range_from.getText().toString().substring(0, 4)), Integer.parseInt(xaxis_range_from
                    .getText().toString().substring(5, 7)) - 1, Integer.parseInt(xaxis_range_from.getText().toString().substring(8, 10))).show();
        });

        selectTimeTo.setOnClickListener(v -> {
            flag2 = 1;
            showTimePickerDialog();
        });

        selectTimeFrom.setOnClickListener(v -> {
            flag2 = 0;
            showTimePickerDialog();
        });

        selectDateTo.setOnClickListener(v -> {
            flag = 1;
            new DatePickerDialog(GraphActivity.this, datePicker, Integer.parseInt(xaxis_range_to.getText().toString().substring(0, 4)), Integer.parseInt(xaxis_range_to.getText()
                    .toString().substring(5, 7)) - 1, Integer.parseInt(xaxis_range_to.getText().toString().substring(8, 10))).show();
        });
    }

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    public void showTimePickerDialog() {

        final Dialog dialog = new Dialog(GraphActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_time_picker);
        actualtime = dialog.findViewById(R.id.actualtime);
        hourplus = dialog.findViewById(R.id.plus1);
        minplus = dialog.findViewById(R.id.plus2);
        secplus = dialog.findViewById(R.id.plus3);
        hourminus = dialog.findViewById(R.id.minus1);
        minminus = dialog.findViewById(R.id.minus2);
        secminus = dialog.findViewById(R.id.minus3);
        hourvalue = dialog.findViewById(R.id.hours);
        minvalue = dialog.findViewById(R.id.minutes);
        secvalue = dialog.findViewById(R.id.seconds);
        Button set = dialog.findViewById(R.id.set);
        Button cancel = dialog.findViewById(R.id.cancel);
        dialog.show();


        int hour = 0, minute = 0, seconds = 0;
        if (flag2 == 0) {
            hour = Integer.parseInt(xaxis_range_from.getText().toString().substring(11, 13));
            minute = Integer.parseInt(xaxis_range_from.getText().toString().substring(14, 16));

            seconds = Integer.parseInt(xaxis_range_from.getText().toString().substring(17, 19));
        } else if (flag2 == 1) {
            hour = Integer.parseInt(xaxis_range_to.getText().toString().substring(11, 13));
            minute = Integer.parseInt(xaxis_range_to.getText().toString().substring(14, 16));

            seconds = Integer.parseInt(xaxis_range_to.getText().toString().substring(17, 19));
        }

        String curhour = String.valueOf(hour);
        String curmin = String.valueOf(minute);
        String cursec = String.valueOf(seconds);
        if (curhour.length() == 1) {
            curhour = "0" + curhour;
        }
        if (curmin.length() == 1) {
            curmin = "0" + curmin;
        }
        if (cursec.length() == 1) {
            cursec = "0" + cursec;
        }

        actualtime.setText(curhour + ":" + curmin + ":" + cursec);
        hourvalue.setText(curhour);
        minvalue.setText(curmin);
        secvalue.setText(cursec);

        cancel.setOnClickListener(v -> dialog.dismiss());

        set.setOnClickListener(v -> {

            String tmphour = hourvalue.getText().toString();
            String tmpmin = minvalue.getText().toString();
            String tmpsec = secvalue.getText().toString();

            actualtime.setText(tmphour + ":" + tmpmin + ":" + tmpsec);

            dialog.dismiss();
            if (flag2 == 0) {
                range_time_from = (tmphour + ":" + tmpmin + ":" + tmpsec);
                xaxis_range_from.setText(range_date_from + " " + range_time_from);
            } else if (flag2 == 1) {
                range_time_to = (tmphour + ":" + tmpmin + ":" + tmpsec);
                xaxis_range_to.setText(range_date_to + " " + range_time_to);
            }

        });

        hourplus.setOnClickListener(v -> {

            hourvalue.requestFocus();
            increment_hr();
        });

        minplus.setOnClickListener(v -> {

            minvalue.requestFocus();
            increment_min();
        });

        secplus.setOnClickListener(v -> {
            secvalue.requestFocus();
            increment_sec();
        });

        hourminus.setOnClickListener(v -> {
            hourvalue.requestFocus();
            decrement_hr();
        });

        minminus.setOnClickListener(v -> {
            minvalue.requestFocus();
            decrement_min();
        });

        secminus.setOnClickListener(v -> {
            secvalue.requestFocus();
            decrement_sec();
        });

        hourplus.setOnLongClickListener(arg0 -> {
            hourvalue.requestFocus();
            mAutoIncrement = true;
            repeatUpdateHandler.post(new RptUpdater());
            return false;
        });

        hourplus.setOnTouchListener((v, event) -> {
            hourvalue.requestFocus();
            if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) && mAutoIncrement) {
                mAutoIncrement = false;
            }
            return false;
        });

        hourminus.setOnLongClickListener(arg0 -> {
            hourvalue.requestFocus();
            mAutoDecrement = true;
            repeatUpdateHandler.post(new RptUpdater());
            return false;
        });

        hourminus.setOnTouchListener((v, event) -> {
            hourvalue.requestFocus();
            if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) && mAutoDecrement) {
                mAutoDecrement = false;
            }

            return false;
        });

        minplus.setOnLongClickListener(arg0 -> {
            minvalue.requestFocus();
            mAutoIncrement = true;
            repeatUpdateHandler.post(new RptUpdater());
            return false;
        });

        minplus.setOnTouchListener((v, event) -> {
            minvalue.requestFocus();
            if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) && mAutoIncrement) {
                mAutoIncrement = false;
            }
            return false;
        });

        minminus.setOnLongClickListener(arg0 -> {
            minvalue.requestFocus();
            mAutoDecrement = true;
            repeatUpdateHandler.post(new RptUpdater());
            return false;
        });

        minminus.setOnTouchListener((v, event) -> {
            minvalue.requestFocus();
            if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) && mAutoDecrement) {
                mAutoDecrement = false;
            }

            return false;
        });

        secplus.setOnLongClickListener(arg0 -> {
            secvalue.requestFocus();
            mAutoIncrement = true;
            repeatUpdateHandler.post(new RptUpdater());
            return false;
        });

        secplus.setOnTouchListener((v, event) -> {
            secvalue.requestFocus();
            if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) && mAutoIncrement) {
                mAutoIncrement = false;
            }
            return false;
        });

        secminus.setOnLongClickListener(arg0 -> {
            secvalue.requestFocus();
            mAutoDecrement = true;
            repeatUpdateHandler.post(new RptUpdater());
            return false;
        });

        secminus.setOnTouchListener((v, event) -> {
            secvalue.requestFocus();
            if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) && mAutoDecrement) {
                mAutoDecrement = false;
            }

            return false;
        });

    }


    private String parseDate(String date) {
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        String[] dateArr = date.split(" ");
        int mnth = -1;
        String month;
        String day;
        String year;
        for (int i = 0; i < 12; i++) {
            if (months[i].equalsIgnoreCase(dateArr[1])) {
                mnth = i + 1;
                break;
            }
        }
        month = Tool.pad(mnth).trim();
        day = dateArr[2].trim();
        year = dateArr[5].trim();

        return year + "-" + month + "-" + day;

    }


    private void openChart() {


        TimeSeries visitsSeries = new TimeSeries(Constant.file_Text);

        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        for (int j = 1; j <= Constant.graph_length; j++) {
            try {
                // Adding data to Visits and Views Series
                visitsSeries.add(dateFormat.parse(Constant.graph_date_time[j - 1]), Constant.graph_data[j - 1]);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // Creating a dataset to hold each series
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

        // Adding Visits Series to the dataset
        dataset.addSeries(visitsSeries);

        // Creating XYSeriesRenderer to customize visitsSeries
        XYSeriesRenderer visitsRenderer = new XYSeriesRenderer();
        visitsRenderer.setColor(Color.YELLOW);
        visitsRenderer.setLineWidth(2);

        // Creating a XYMultipleSeriesRenderer to customize the whole chart
        XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
        multiRenderer.setApplyBackgroundColor(true);
        multiRenderer.setMarginsColor(Color.parseColor("#227799"));
        multiRenderer.setBackgroundColor(Color.parseColor("#227799"));
        multiRenderer.setChartTitle("");
        multiRenderer.setXTitle("\n\n\n\n\nDate/Time");
        multiRenderer.setYTitle(Constant.file_Text);
        multiRenderer.setYLabelsAlign(Paint.Align.RIGHT);
        multiRenderer.setZoomButtonsVisible(true);
        multiRenderer.setMargins(new int[]{10, 90, 90, 10});// top,left,Bottom,Right
        multiRenderer.setLegendTextSize(20);
        multiRenderer.setAxisTitleTextSize(15);
        multiRenderer.setChartTitleTextSize(20);
        multiRenderer.setXLabelsPadding(2);
        multiRenderer.setYLabelsPadding(5);
        multiRenderer.setAxesColor(Color.WHITE);
        multiRenderer.setLabelsColor(Color.WHITE);
        multiRenderer.setXLabelsColor(Color.GREEN);
        multiRenderer.setYLabelsColor(0, Color.GREEN);
//        multiRenderer.setXLabelsAngle(270);
        multiRenderer.setShowLegend(false);

        double range_retrieve = set_graph_range(Double.parseDouble(yaxis_range_max.getText().toString().trim()), Double.parseDouble(yaxis_range_min.getText().toString().trim()));

        double graph_max = Double.parseDouble(yaxis_range_max.getText().toString()) + range_retrieve;
        double graph_min = Double.parseDouble(yaxis_range_min.getText().toString()) - range_retrieve;
        if (graph_min > Constant.graph_min) {
            graph_min = Constant.graph_min - range_retrieve;
        }
        if (graph_max < Constant.graph_max) {
            graph_max = Constant.graph_max + range_retrieve;
        }
        multiRenderer.setYAxisMin(graph_min);
        multiRenderer.setYAxisMax(graph_max);

        multiRenderer.setXAxisMin(Double.parseDouble(Constant.minDate));
        multiRenderer.setXAxisMax(Double.parseDouble(Constant.maxDate));

        // Adding visitsRenderer and viewsRenderer to multipleRenderer
        // Note: The order of adding dataseries to dataset and renderers to
        // multipleRenderer
        // should be same

        for (int i = 0; i < Constant.graph_length; i++) {
            multiRenderer.addXTextLabel(i, Constant.graph_date_time[i]);
        }

        multiRenderer.setLabelsTextSize(15);
        multiRenderer.addXTextLabel(3, Constant.file_Text);
        multiRenderer.setGridColor(Color.argb(170, 165, 165, 150));
        multiRenderer.setShowGrid(true);
        multiRenderer.addSeriesRenderer(visitsRenderer);

        // Getting a reference to LinearLayout of the MainActivity Layout
        LinearLayout chartContainer = findViewById(R.id.chart);

        // Creating a Time Chart
        mChart = ChartFactory.getTimeChartView(getBaseContext(), dataset, multiRenderer, "yyyy/MM/dd\nHH:mm");

        multiRenderer.setClickEnabled(true);
        multiRenderer.setSelectableBuffer(0);

        // Setting a click event listener for the graph
        mChart.setOnClickListener(v -> {
            @SuppressLint("SimpleDateFormat") Format formatter = new SimpleDateFormat("yyyy/MM/dd\nHH:mm");

            SeriesSelection seriesSelection = mChart.getCurrentSeriesAndPoint();

            if (seriesSelection != null) {
                int seriesIndex = seriesSelection.getSeriesIndex();
                String selectedSeries = Constant.file_Text;
                if (seriesIndex == 0)
                    selectedSeries = Constant.file_Text;
                /*
                 * else selectedSeries = "Views";
                 */
                // Getting the clicked Date ( x value )
                long clickedDateSeconds = (long) seriesSelection.getXValue();
                Date clickedDate = new Date(clickedDateSeconds);
                String strDate = formatter.format(clickedDate);

                // Getting the y value
                int amount = (int) seriesSelection.getValue();

                // Displaying Toast Message
                Toast.makeText(getBaseContext(), selectedSeries + " on " + strDate + " : " + amount, Toast.LENGTH_SHORT).show();
            }
        });

        // Adding the Line Chart to the LinearLayout
        chartContainer.addView(mChart);
    }

    @SuppressLint("SetTextI18n")
    public void decrement_hr() {

        String tmphour = hourvalue.getText().toString();
        int curdisplayhr = Integer.parseInt(tmphour);
        int finaldisplayhr = 0;

        if (curdisplayhr == 0) {
            finaldisplayhr = 23;
        } else if (curdisplayhr > 1 || curdisplayhr < 24) {
            finaldisplayhr = curdisplayhr - 1;
        }
        String finalhr = String.valueOf(finaldisplayhr);
        if (finalhr.length() == 1) {
            finalhr = "0" + finalhr;
        }


        hourvalue.setText(finalhr);

        String tmpmin = minvalue.getText().toString();
        String tmpsec = secvalue.getText().toString();

        actualtime.setText(finalhr + ":" + tmpmin + ":" + tmpsec);

    }

    @SuppressLint("SetTextI18n")
    public void increment_hr() {


        String tmphour = hourvalue.getText().toString();
        int curdisplayhr = Integer.parseInt(tmphour);
        int finaldisplayhr = 0;
        if (curdisplayhr < 23) {
            finaldisplayhr = curdisplayhr + 1;
        } else if (curdisplayhr == 24) {
            finaldisplayhr = 0;
        }
        String finalhr = String.valueOf(finaldisplayhr);
        if (finalhr.length() == 1) {
            finalhr = "0" + finalhr;
        }

        hourvalue.setText(finalhr);

        String tmpmin = minvalue.getText().toString();
        String tmpsec = secvalue.getText().toString();

        actualtime.setText(finalhr + ":" + tmpmin + ":" + tmpsec);

    }

    @SuppressLint("SetTextI18n")
    public void increment_min() {


        String tmpmin = minvalue.getText().toString();
        int curdisplaymin = Integer.parseInt(tmpmin);
        int finaldisplaymin = 0;
        if (curdisplaymin < 59) {
            finaldisplaymin = curdisplaymin + 1;
        } else if (curdisplaymin == 59) {
            finaldisplaymin = 0;
        }
        String finalmin = String.valueOf(finaldisplaymin);
        if (finalmin.length() == 1) {
            finalmin = "0" + finalmin;
        }
        minvalue.setText(finalmin);
        String tmphour = hourvalue.getText().toString();
        String tmpsec = secvalue.getText().toString();

        actualtime.setText(tmphour + ":" + finalmin + ":" + tmpsec);

    }

    @SuppressLint("SetTextI18n")
    public void decrement_min() {

        String tmpmin = minvalue.getText().toString();
        int curdisplaymin = Integer.parseInt(tmpmin);
        int finaldisplaymin;
        if (curdisplaymin == 0) {
            finaldisplaymin = 59;
        } else {
            finaldisplaymin = curdisplaymin - 1;
        }

        String finalmin = String.valueOf(finaldisplaymin);
        if (finalmin.length() == 1) {
            finalmin = "0" + finalmin;
        }
        minvalue.setText(finalmin);
        String tmphour = hourvalue.getText().toString();
        String tmpsec = secvalue.getText().toString();

        actualtime.setText(tmphour + ":" + finalmin + ":" + tmpsec);

    }

    @SuppressLint("SetTextI18n")
    public void increment_sec() {

        String tmpsec = secvalue.getText().toString();
        int curdisplaysec = Integer.parseInt(tmpsec);
        int finaldisplaysec = 0;
        if (curdisplaysec < 59) {
            finaldisplaysec = curdisplaysec + 1;
        } else if (curdisplaysec == 59) {
            finaldisplaysec = 0;
        }
        String finalsec = String.valueOf(finaldisplaysec);
        if (finalsec.length() == 1) {
            finalsec = "0" + finalsec;
        }
        secvalue.setText(finalsec);
        String tmphour = hourvalue.getText().toString();
        String tmpmin = minvalue.getText().toString();

        actualtime.setText(tmphour + ":" + tmpmin + ":" + finalsec);

    }

    @SuppressLint("SetTextI18n")
    public void decrement_sec() {

        String tmpsec = secvalue.getText().toString();
        int curdisplaysec = Integer.parseInt(tmpsec);
        int finaldisplaysec;
        if (curdisplaysec == 0) {
            finaldisplaysec = 59;
        } else {
            finaldisplaysec = curdisplaysec - 1;
        }
        String finalsec = String.valueOf(finaldisplaysec);
        if (finalsec.length() == 1) {
            finalsec = "0" + finalsec;
        }
        secvalue.setText(finalsec);
        String tmphour = hourvalue.getText().toString();
        String tmpmin = minvalue.getText().toString();

        actualtime.setText(tmphour + ":" + tmpmin + ":" + finalsec);

    }

    private boolean validateFromDate(String dateParse) {

        try {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date d1 = sdf.parse(dataRange_from.getText().toString());
            Date d2 = sdf.parse(dateParse);
            Date d3 = sdf.parse(dataRange_to.getText().toString());

            assert d2 != null;
            Constant.minDate = String.valueOf(d2.getTime());
            Constant.Date_min = sdf.format(d2);
            if (d2.compareTo(d3) > 0) {

                exceptionSetup = true;
                err_msg = "Begin Date/Time exceeded the End Data Range";
                return exceptionSetup;
            } else if (d2.compareTo(d1) < 0) {

                exceptionSetup = true;
                err_msg = "Begin Date/Time should not exceed Initial Data Range";
                return exceptionSetup;
            } else if (d2.compareTo(d3) == 0) {

                exceptionSetup = true;
                err_msg = "Begin Date/Time should not be equal to End Data Range";
                return exceptionSetup;
            }

        } catch (Exception e) {
            exceptionSetup = true;
            err_msg = "Error in Text Field Values...";
            return exceptionSetup;
        }
        return false;
    }

    private boolean validateToDate(String dateParse) {

        try {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date d1 = sdf.parse(dataRange_from.getText().toString());
            Date d2 = sdf.parse(dateParse);
            Date d3 = sdf.parse(dataRange_to.getText().toString());
            Date d4 = sdf.parse(xaxis_range_from.getText().toString());
            assert d2 != null;
            Constant.maxDate = String.valueOf(d2.getTime());
            Constant.Date_max = sdf.format(d2);


            if (d2.compareTo(d3) > 0) {

                exceptionSetup = true;
                err_msg = "End Date/Time exceeded the End limit Data Range";
                return exceptionSetup;

            } else if (d2.compareTo(d4) < 0) {

                exceptionSetup = true;
                err_msg = "End Date/Time should not be Before the Initial Date/Time";
                return exceptionSetup;
            } else if (d2.compareTo(d4) == 0) {
                exceptionSetup = true;
                err_msg = "End Date/Time should be greater than Initial Date/Time";
                return exceptionSetup;
            } else if (d2.compareTo(d1) == 0) {

                exceptionSetup = true;
                err_msg = "End Date/Time should be greater than Initial Data Range";
                return exceptionSetup;
            } else if (d2.compareTo(d1) < 0) {
                exceptionSetup = true;
                err_msg = "End Date/Time should be greater than Initial Data Range";
                return exceptionSetup;
            }

        } catch (Exception e) {
            exceptionSetup = true;
            err_msg = "Error in Text Field Values...";
            return exceptionSetup;
        }
        return false;
    }


    private Double set_graph_range(double max, double min) {
        double range;
        if (max == min) {
            range = Math.abs(max) * 0.025;
        } else {
            range = Math.abs(max - min) * 0.025;
        }

        return range;

    }


    class RptUpdater implements Runnable {
        public void run() {
            if (mAutoIncrement) {
                if (minplus.isPressed()) {
                    increment_min();
                } else if (secplus.isPressed()) {
                    increment_sec();
                } else if (hourplus.isPressed()) {
                    increment_hr();
                }
                repeatUpdateHandler.postDelayed(new RptUpdater(), 100);
            } else if (mAutoDecrement) {
                if (hourminus.isPressed()) {
                    decrement_hr();
                } else if (minminus.isPressed()) {
                    decrement_min();
                } else if (secminus.isPressed()) {
                    decrement_sec();
                }

                repeatUpdateHandler.postDelayed(new RptUpdater(), 100);
            }
        }
    }


}
