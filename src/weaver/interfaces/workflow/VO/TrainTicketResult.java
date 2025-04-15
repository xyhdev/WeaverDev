package weaver.interfaces.workflow.VO;

public class TrainTicketResult
{
    // 车票号
    private String ticket_num ;
    // 始发站
    private String starting_station ;
    // 车次号
    private String train_num ;
    // 	到达站
    private String destination_station ;
    // 出发日期
    private String date ;
    // 车票金额
    private String ticket_rates ;
    // 席别
    private String seat_category ;
    // 乘客姓名
    private String name ;
    // 身份证号
    private String id_num ;
    // 序列号
    private String serial_number ;
    // 售站
    private String sales_station ;
    // 时间
    private String time ;
    // 座位号
    private String seat_num ;
    // 发票号码，仅在输入为电子火车票时返回该字段
    public String invoice_num ;
    // 电子客票号，仅在输入为电子火车票时返回该字段
    public String elec_ticket_num ;
}