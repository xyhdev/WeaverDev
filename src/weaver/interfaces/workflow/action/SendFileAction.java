package weaver.interfaces.workflow.action;

import weaver.conn.RecordSet;
import weaver.file.ImageFileManager;
import weaver.soa.workflow.request.RequestInfo;
import weaverjn.util.TicketHttpClient;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 火车票和发票自动识别及报销处理
 * 高性能优化版本：减少资源占用，处理两种类型附件
 */
public class SendFileAction implements Action {

    // 常量定义
    private static final String SERVER_IP = "http://192.168.7.75:8088";
    private static final String TICKET_API_URL = "http://192.168.7.42:8086/api/invoice/upload";
    private static final String CALC_API_URL = "http://192.168.7.42:8086/api/invoice/run";

    @Override
    public String execute(RequestInfo request) {
        RecordSet rs = new RecordSet();

        try {
            // 获取表单数据
            String requestId = request.getRequestid();
            String tablename = request.getRequestManager().getBillTableName();

            rs.execute("select id, zj, ts, hcp, zsfp from " + tablename + " where requestid = " + requestId);
            if (!rs.next()) {
                return Action.SUCCESS;
            }

            String mainId = rs.getString("id");
            String positionCode = rs.getString("zj");
            String days = rs.getString("ts");
            String hcp = rs.getString("hcp");
            String zsfp = rs.getString("zsfp");

            // 处理火车票附件
            List<Integer> trainFileIds = getAttachmentIds(rs, hcp);
            List<String> trainFileUrls = getFileDownUrls(trainFileIds);

            // 处理发票附件
            List<Integer> vatFileIds = getAttachmentIds(rs, zsfp);
            List<String> vatFileUrls = getFileDownUrls(vatFileIds);

            // 一次性调用接口处理两种票据
            processAllAttachments(rs, tablename, requestId, mainId, positionCode,
                    days, trainFileUrls, vatFileUrls);

        } catch (Exception e) {
            return Action.FAILURE_AND_CONTINUE;
        }

        return Action.SUCCESS;
    }

    /**
     * 获取附件ID列表
     */
    private List<Integer> getAttachmentIds(RecordSet rs, String attachIds) {
        List<Integer> fileIds = new ArrayList<>();

        if (attachIds == null || attachIds.isEmpty()) {
            return fileIds;
        }

        try {
            rs.execute("select imagefileid from DocImageFile where docid in (" + attachIds + ")");
            while (rs.next()) {
                fileIds.add(rs.getInt("imagefileid"));
            }
        } catch (Exception e) {
            // 忽略错误
        }

        return fileIds;
    }

    /**
     * 获取文件下载URL
     */
    private List<String> getFileDownUrls(List<Integer> fileIds) throws Exception {
        List<String> urls = new ArrayList<>();

        if (fileIds.isEmpty()) {
            return urls;
        }

        weaver.docs.docs.util.DesUtils des = new weaver.docs.docs.util.DesUtils();

        for (Integer fileId : fileIds) {
            try {
                String ddcode = "1_" + fileId;
                ddcode = des.encrypt(ddcode);
                String fileDownUrl = String.format(
                        "%s/weaver/weaver.file.FileDownload?fileid=%s&download=1&ddcode=%s",
                        SERVER_IP, fileId, ddcode
                );
                urls.add(fileDownUrl);
            } catch (Exception e) {
                // 忽略单个URL生成错误
            }
        }

        return urls;
    }

    /**
     * 一次性处理所有附件
     */
    private void processAllAttachments(RecordSet rs, String tablename, String requestId, String mainId,
                                       String positionCode, String days,
                                       List<String> trainUrls, List<String> vatUrls) {
        if (trainUrls.isEmpty() && vatUrls.isEmpty()) {
            return;
        }

        TicketHttpClient httpClient = new TicketHttpClient();

        try {
            // 构建火车票JSON
            StringBuilder trainJson = new StringBuilder();
            for (int i = 0; i < trainUrls.size(); i++) {
                if (i > 0) trainJson.append(",");
                trainJson.append("\"").append(trainUrls.get(i)).append("\"");
            }

            // 构建发票JSON
            StringBuilder vatJson = new StringBuilder();
            for (int i = 0; i < vatUrls.size(); i++) {
                if (i > 0) vatJson.append(",");
                vatJson.append("\"").append(vatUrls.get(i)).append("\"");
            }

            // 发送请求
            String jsonBody = "[{" +
                    "\"ticketType\": 0," +
                    "\"files\": [" + trainJson.toString() + "]" +
                    "},{" +
                    "\"ticketType\": 2," +
                    "\"files\": [" + vatJson.toString() + "]" +
                    "}]";

            String response = httpClient.sendTicketData(TICKET_API_URL, jsonBody);

            if (response == null || response.isEmpty()) {
                return;
            }

            // 解析响应
            com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSON.parseObject(response);

            if (!jsonObject.getBooleanValue("success")) {
                return;
            }

            com.alibaba.fastjson.JSONObject data = jsonObject.getJSONObject("data");

            // 处理火车票数据
            double totalTrainAmount = 0.0;
            // 新增发票总额变量
            double totalVatAmount = 0.0;
            String lastDestination = "未知";

            if (data.containsKey("trains")) {
                com.alibaba.fastjson.JSONArray trains = data.getJSONArray("trains");
                if (trains != null && !trains.isEmpty()) {
                    List<Map<String, String>> allTickets = new ArrayList<>();

                    // 处理所有火车票
                    for (int i = 0; i < trains.size(); i++) {
                        com.alibaba.fastjson.JSONObject train = trains.getJSONObject(i);

                        // 创建票据数据
                        Map<String, String> ticket = new HashMap<>();
                        ticket.put("name", train.getString("name"));

                        String date = train.getString("date");
                        String time = train.getString("time");
                        String dateTime = (date != null ? date : "") + (time != null ? " " + time : "");
                        ticket.put("dateTime", dateTime);

                        ticket.put("startStation", train.getString("starting_station"));
                        ticket.put("destStation", train.getString("destination_station"));
                        ticket.put("trainNum", train.getString("train_num"));
                        ticket.put("amount", train.getString("ticket_rates"));
                        ticket.put("invoiceNum", train.getString("invoice_num"));
                        ticket.put("seatType", train.getString("seat_category"));

                        // 添加到结果集
                        allTickets.add(ticket);

                        // 累加金额
                        String ticketRate = train.getString("ticket_rates");
                        if (ticketRate != null && !ticketRate.isEmpty()) {
                            try {
                                totalTrainAmount += Double.parseDouble(ticketRate);
                            } catch (NumberFormatException e) {
                                // 忽略无法解析的金额
                            }
                        }

                        // 记录最后一个目的地
                        String destStation = train.getString("destination_station");
                        if (i == trains.size() - 1 && destStation != null && !destStation.isEmpty()) {
                            lastDestination = destStation;
                        }
                    }

                    // 保存火车票数据
                    if (!allTickets.isEmpty()) {
                        saveBatchTrainTickets(rs, tablename, mainId, allTickets);
                    }
                }
            }

            // 处理发票数据
            if (data.containsKey("vats")) {
                com.alibaba.fastjson.JSONArray vats = data.getJSONArray("vats");
                if (vats != null && !vats.isEmpty()) {
                    List<Map<String, String>> allVats = new ArrayList<>();

                    // 处理所有发票
                    for (int i = 0; i < vats.size(); i++) {
                        com.alibaba.fastjson.JSONObject vat = vats.getJSONObject(i);

                        // 创建发票数据
                        Map<String, String> vatData = new HashMap<>();
                        vatData.put("gmfmc", vat.getString("purchaserName"));
                        vatData.put("gmfhm", vat.getString("purchaserRegisterNum"));

                        // 处理项目名称列表
                        String commodityNames = "";
                        com.alibaba.fastjson.JSONArray commodityArray = vat.getJSONArray("commodityName");
                        if (commodityArray != null && !commodityArray.isEmpty()) {
                            // 只用简单字符串
                            for (int j = 0; j < commodityArray.size(); j++) {
                                com.alibaba.fastjson.JSONObject item = commodityArray.getJSONObject(j);
                                String word = item.getString("word");
                                // 检查不是null和非"null"
                                if (word != null && !word.equals("null")) {
                                    if (j > 0 && !commodityNames.isEmpty()) commodityNames += ", ";
                                    commodityNames += word;
                                }
                            }
                        }
                        vatData.put("xmmc", commodityNames);

                        // 获取发票金额
                        String amountStr = vat.getString("amountInFiguers");
                        vatData.put("je", amountStr);

                        // 累加发票金额
                        if (amountStr != null && !amountStr.isEmpty()) {
                            try {
                                totalVatAmount += Double.parseDouble(amountStr);
                            } catch (NumberFormatException e) {
                                // 忽略无法解析的金额
                            }
                        }

                        vatData.put("fphm", vat.getString("invoiceNum"));
                        vatData.put("kprq", vat.getString("invoiceDate"));
                        vatData.put("xsfmc", vat.getString("sellerName"));

                        // 添加到结果集
                        allVats.add(vatData);
                    }

                    // 保存发票数据
                    if (!allVats.isEmpty()) {
                        saveBatchVats(rs, tablename, mainId, allVats);
                    }
                }
            }

            // 修改判断条件：任一金额大于0都进行计算
            if (totalTrainAmount > 0 || totalVatAmount > 0) {
                String positionName = mapPositionToName(positionCode);
                calculateAndUpdateExpenseAmount(rs, tablename, requestId, positionName,
                        lastDestination, days, totalTrainAmount, totalVatAmount);
            }

        } catch (Exception e) {
            // 忽略处理错误
        }
    }

    /**
     * 批量保存火车票到dt3表
     */
    private void saveBatchTrainTickets(RecordSet rs, String tablename, String mainId,
                                       List<Map<String, String>> tickets) {
        if (tickets.isEmpty()) {
            return;
        }

        // 构建批量插入SQL
        StringBuilder batchSql = new StringBuilder();
        batchSql.append("INSERT INTO ").append(tablename).append("_dt3")
                .append(" (mainid, xm, ccrq, sfd, mdd, cc, je, pjhm, zwlx) VALUES ");

        boolean first = true;
        for (Map<String, String> ticket : tickets) {
            if (!first) {
                batchSql.append(",");
            }

            batchSql.append("(")
                    .append(mainId).append(",")
                    .append("'").append(escapeSQL(ticket.get("name"))).append("',")
                    .append("'").append(escapeSQL(ticket.get("dateTime"))).append("',")
                    .append("'").append(escapeSQL(ticket.get("startStation"))).append("',")
                    .append("'").append(escapeSQL(ticket.get("destStation"))).append("',")
                    .append("'").append(escapeSQL(ticket.get("trainNum"))).append("',")
                    .append("'").append(escapeSQL(ticket.get("amount"))).append("',")
                    .append("'").append(escapeSQL(ticket.get("invoiceNum"))).append("',")
                    .append("'").append(escapeSQL(ticket.get("seatType"))).append("'")
                    .append(")");

            first = false;
        }

        try {
            // 执行批量插入
            rs.execute(batchSql.toString());
        } catch (Exception e) {
            // 批量插入失败，尝试逐条插入
            for (Map<String, String> ticket : tickets) {
                try {
                    String sql = "INSERT INTO " + tablename + "_dt3" +
                            " (mainid, xm, ccrq, sfd, mdd, cc, je, pjhm, zwlx) VALUES (" +
                            mainId + "," +
                            "'" + escapeSQL(ticket.get("name")) + "'," +
                            "'" + escapeSQL(ticket.get("dateTime")) + "'," +
                            "'" + escapeSQL(ticket.get("startStation")) + "'," +
                            "'" + escapeSQL(ticket.get("destStation")) + "'," +
                            "'" + escapeSQL(ticket.get("trainNum")) + "'," +
                            "'" + escapeSQL(ticket.get("amount")) + "'," +
                            "'" + escapeSQL(ticket.get("invoiceNum")) + "'," +
                            "'" + escapeSQL(ticket.get("seatType")) + "'" +
                            ")";
                    rs.execute(sql);
                } catch (Exception ex) {
                    // 忽略单条插入错误
                }
            }
        }
    }

    /**
     * 批量保存发票到dt5表
     */
    private void saveBatchVats(RecordSet rs, String tablename, String mainId,
                               List<Map<String, String>> vats) {
        if (vats.isEmpty()) {
            return;
        }

        // 构建批量插入SQL
        StringBuilder batchSql = new StringBuilder();
        batchSql.append("INSERT INTO ").append(tablename).append("_dt5")
                .append(" (mainid, gmfmc, gmfhm, xmmc, je, fphm, kprq, xsfmc) VALUES ");

        boolean first = true;
        for (Map<String, String> vat : vats) {
            if (!first) {
                batchSql.append(",");
            }

            batchSql.append("(")
                    .append(mainId).append(",")
                    .append("'").append(escapeSQL(vat.get("gmfmc"))).append("',")
                    .append("'").append(escapeSQL(vat.get("gmfhm"))).append("',")
                    .append("'").append(escapeSQL(vat.get("xmmc"))).append("',")
                    .append("'").append(escapeSQL(vat.get("je"))).append("',")
                    .append("'").append(escapeSQL(vat.get("fphm"))).append("',")
                    .append("'").append(escapeSQL(vat.get("kprq"))).append("',")
                    .append("'").append(escapeSQL(vat.get("xsfmc"))).append("'")
                    .append(")");

            first = false;
        }

        try {
            // 执行批量插入
            rs.execute(batchSql.toString());
        } catch (Exception e) {
            // 批量插入失败，尝试逐条插入
            for (Map<String, String> vat : vats) {
                try {
                    String sql = "INSERT INTO " + tablename + "_dt5" +
                            " (mainid, gmfmc, gmfhm, xmmc, je, fphm, kprq, xsfmc) VALUES (" +
                            mainId + "," +
                            "'" + escapeSQL(vat.get("gmfmc")) + "'," +
                            "'" + escapeSQL(vat.get("gmfhm")) + "'," +
                            "'" + escapeSQL(vat.get("xmmc")) + "'," +
                            "'" + escapeSQL(vat.get("je")) + "'," +
                            "'" + escapeSQL(vat.get("fphm")) + "'," +
                            "'" + escapeSQL(vat.get("kprq")) + "'," +
                            "'" + escapeSQL(vat.get("xsfmc")) + "'" +
                            ")";
                    rs.execute(sql);
                } catch (Exception ex) {
                    // 忽略单条插入错误
                }
            }
        }
    }

    /**
     * 计算并更新报销金额
     * 修改为同时接收火车票和发票金额
     */
    private void calculateAndUpdateExpenseAmount(RecordSet rs, String tablename, String requestId,
                                                 String positionName, String location, String days,
                                                 double trainAmount, double vatAmount) {
        try {
            TicketHttpClient httpClient = new TicketHttpClient();

            // 构建请求URL，添加发票金额参数
            String calcUrl = CALC_API_URL +
                    "?position=" + java.net.URLEncoder.encode(positionName, "UTF-8") +
                    "&location=" + java.net.URLEncoder.encode(location, "UTF-8") +
                    "&day=" + days +
                    "&train=" + java.net.URLEncoder.encode(String.valueOf(trainAmount), "UTF-8") +
                    "&vat=" + java.net.URLEncoder.encode(String.valueOf(vatAmount), "UTF-8") +
                    "&air=0";

            // 调用计算接口
            String calcResponse = httpClient.sendGetRequest(calcUrl);

            if (calcResponse != null && !calcResponse.isEmpty()) {
                // 解析响应
                com.alibaba.fastjson.JSONObject calcResult = com.alibaba.fastjson.JSON.parseObject(calcResponse);

                if (calcResult.getBooleanValue("success")) {
                    // 获取金额
                    String expenseAmount = null;
                    Object dataObj = calcResult.get("data");

                    if (dataObj != null) {
                        if (dataObj instanceof String) {
                            expenseAmount = (String) dataObj;
                        } else if (dataObj instanceof Number) {
                            expenseAmount = dataObj.toString();
                        } else if (dataObj instanceof com.alibaba.fastjson.JSONObject) {
                            com.alibaba.fastjson.JSONObject dataJson = (com.alibaba.fastjson.JSONObject) dataObj;
                            if (dataJson.containsKey("total")) {
                                expenseAmount = dataJson.getString("total");
                            }
                        }
                    }

                    // 更新主表
                    if (expenseAmount != null && !expenseAmount.isEmpty()) {
                        rs.execute("UPDATE " + tablename +
                                " SET bxzje = '" + escapeSQL(expenseAmount) + "'" +
                                " WHERE requestid = " + requestId);
                    }
                }
            }
        } catch (Exception e) {
            // 忽略计算错误
        }
    }

    /**
     * 根据职位代码获取职位名称
     */
    private String mapPositionToName(String positionCode) {
        if (positionCode == null) {
            return "业务员"; // 默认职位
        }

        switch (positionCode) {
            case "0": return "业务员";
            case "1": return "部门负责人";
            case "2": return "副总";
            case "3": return "总经理";
            default: return "业务员";
        }
    }

    /**
     * SQL值转义（防SQL注入）
     */
    private String escapeSQL(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("'", "''");
    }

    /**
     * 获取文件管理器
     */
    public ImageFileManager getFile(int imagefileid) {
        ImageFileManager imageFileManager = new ImageFileManager();
        imageFileManager.getImageFileInfoById(imagefileid);
        return imageFileManager;
    }
}