package com.api.workflow.yytex.Invoice.controller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import weaver.conn.RecordSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 火车票和发票自动识别及报销处理 RESTful 服务
 */
@Path("/workflow/yytex")
public class InvoiceRecognition {

    // 常量定义
    private static final String SERVER_IP = "http://192.168.7.75:8088";
    private static final String TICKET_API_URL = "http://192.168.7.42:8086/api/invoice/upload";

    /**
     * 票据识别处理 RESTful 端点 - 基于附件ID处理
     * 接收火车票和发票附件ID列表，进行识别并返回结果
     */
    @POST
    @Path("/processAttachments")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response processAttachments(String jsonInput) throws JSONException {
        try {
            JSONObject inputData = new JSONObject(jsonInput);

            // 获取请求参数
            String trainAttachIds = inputData.optString("trainAttachIds");
            String vatAttachIds = inputData.optString("vatAttachIds");

            // 验证必填参数
            if (!inputData.has("positionCode")) {
                JSONObject errorResponse = new JSONObject();
                errorResponse.put("success", false);
                errorResponse.put("message", "缺少必填参数:(职位代码)");
                return Response.status(400).entity(errorResponse.toString()).build();
            }

            if (!inputData.has("days")) {
                JSONObject errorResponse = new JSONObject();
                errorResponse.put("success", false);
                errorResponse.put("message", "缺少必填参数:(报销天数)");
                return Response.status(400).entity(errorResponse.toString()).build();
            }

            if (!inputData.has("city")) {
                JSONObject errorResponse = new JSONObject();
                errorResponse.put("success", false);
                errorResponse.put("message", "缺少必填参数: city (出差城市)");
                return Response.status(400).entity(errorResponse.toString()).build();
            }

            String positionCode = inputData.getString("positionCode");
            int days = Integer.parseInt(inputData.getString("days"));
            String city = inputData.getString("city");

            // 校验至少有一种票据
            if ((trainAttachIds == null || trainAttachIds.isEmpty()) &&
                    (vatAttachIds == null || vatAttachIds.isEmpty())) {
                JSONObject errorResponse = new JSONObject();
                errorResponse.put("success", false);
                errorResponse.put("message", "请至少提供一种票据附件ID");
                return Response.status(400).entity(errorResponse.toString()).build();
            }

            // 处理所有附件
            JSONObject result = processAllAttachmentIds(trainAttachIds, vatAttachIds, positionCode, days, city);
            return Response.ok(result.toString(), MediaType.APPLICATION_JSON).build();

        } catch (Exception e) {
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("success", false);
            errorResponse.put("message", "处理失败: " + e.getMessage());
            return Response.status(500).entity(errorResponse.toString()).build();
        }
    }

    /**
     * 简单测试端点
     */
    @GET
    @Path("/test")
    @Produces(MediaType.TEXT_PLAIN)
    public String test(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        return "服务运行正常 - " + new java.util.Date();
    }

    /**
     * 处理所有附件ID
     */
    private JSONObject processAllAttachmentIds(String trainAttachIds, String vatAttachIds,
                                               String positionCode, int days, String city) throws JSONException {
        JSONObject response = new JSONObject();
        RecordSet rs = new RecordSet();

        try {
            // 处理火车票附件
            List<Integer> trainFileIds = getAttachmentIds(rs, trainAttachIds);
            List<String> trainFileUrls = getFileDownUrls(trainFileIds);

            // 处理发票附件
            List<Integer> vatFileIds = getAttachmentIds(rs, vatAttachIds);
            List<String> vatFileUrls = getFileDownUrls(vatFileIds);

            if (trainFileUrls.isEmpty() && vatFileUrls.isEmpty()) {
                response.put("success", false);
                response.put("message", "无法获取任何有效文件");
                return response;
            }

            // 构建火车票JSON
            StringBuilder trainJson = new StringBuilder();
            for (int i = 0; i < trainFileUrls.size(); i++) {
                if (i > 0) trainJson.append(",");
                trainJson.append("\"").append(trainFileUrls.get(i)).append("\"");
            }

            // 构建发票JSON
            StringBuilder vatJson = new StringBuilder();
            for (int i = 0; i < vatFileUrls.size(); i++) {
                if (i > 0) vatJson.append(",");
                vatJson.append("\"").append(vatFileUrls.get(i)).append("\"");
            }

            // 构建请求体
            String jsonBody = "[{" +
                    "\"ticketType\": 0," +
                    "\"files\": [" + trainJson.toString() + "]" +
                    "},{" +
                    "\"ticketType\": 2," +
                    "\"files\": [" + vatJson.toString() + "]" +
                    "}]";

            // 发送识别请求
            weaverjn.util.TicketHttpClient httpClient = new weaverjn.util.TicketHttpClient();
            String recognitionResponse = httpClient.sendTicketData(TICKET_API_URL, jsonBody);

            if (recognitionResponse == null || recognitionResponse.isEmpty()) {
                response.put("success", false);
                response.put("message", "识别服务未返回结果");
                return response;
            }

            // 解析响应
            com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSON.parseObject(recognitionResponse);

            if (!jsonObject.getBooleanValue("success")) {
                response.put("success", false);
                response.put("message", "识别服务处理失败");
                return response;
            }

            // 提取响应数据
            com.alibaba.fastjson.JSONObject data = jsonObject.getJSONObject("data");

            // 处理票据数据
            double totalTrainAmount = 0.0;
            double totalVatAmount = 0.0;


            JSONArray trainTickets = new JSONArray();
            JSONArray vatReceipts = new JSONArray();

            // 处理火车票数据
            if (data.containsKey("trains")) {
                com.alibaba.fastjson.JSONArray trains = data.getJSONArray("trains");
                if (trains != null && !trains.isEmpty()) {
                    for (int i = 0; i < trains.size(); i++) {
                        com.alibaba.fastjson.JSONObject train = trains.getJSONObject(i);

                        // 创建票据数据
                        JSONObject ticket = new JSONObject();
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
                        trainTickets.put(ticket);

                        // 累加金额
                        String ticketRate = train.getString("ticket_rates");
                        if (ticketRate != null && !ticketRate.isEmpty()) {
                            try {
                                // 移除非数字字符（保留数字和小数点）
                                String numericValue = ticketRate.replaceAll("[^0-9.]", "");
                                if (!numericValue.isEmpty()) {
                                    totalTrainAmount += Double.parseDouble(numericValue);
                                }
                            } catch (NumberFormatException e) {
                                // 忽略无法解析的金额
                            }
                        }

                    }
                }
            }

            // 处理发票数据
            if (data.containsKey("vats")) {
                com.alibaba.fastjson.JSONArray vats = data.getJSONArray("vats");
                if (vats != null && !vats.isEmpty()) {
                    for (int i = 0; i < vats.size(); i++) {
                        com.alibaba.fastjson.JSONObject vat = vats.getJSONObject(i);

                        // 创建发票数据
                        JSONObject vatData = new JSONObject();
                        vatData.put("purchaserName", vat.getString("purchaserName"));
                        vatData.put("purchaserRegisterNum", vat.getString("purchaserRegisterNum"));

                        // 处理项目名称列表
                        String commodityNames = "";
                        com.alibaba.fastjson.JSONArray commodityArray = vat.getJSONArray("commodityName");
                        if (commodityArray != null && !commodityArray.isEmpty()) {
                            for (int j = 0; j < commodityArray.size(); j++) {
                                com.alibaba.fastjson.JSONObject item = commodityArray.getJSONObject(j);
                                String word = item.getString("word");
                                if (word != null && !word.equals("null")) {
                                    if (j > 0 && !commodityNames.isEmpty()) commodityNames += ", ";
                                    commodityNames += word;
                                }
                            }
                        }
                        vatData.put("commodityName", commodityNames);

                        // 获取发票金额
                        String amountStr = vat.getString("amountInFiguers");
                        vatData.put("amount", amountStr);

                        // 累加发票金额
                        if (amountStr != null && !amountStr.isEmpty()) {
                            try {
                                // 移除非数字字符（保留数字和小数点）
                                String numericValue = amountStr.replaceAll("[^0-9.]", "");
                                if (!numericValue.isEmpty()) {
                                    totalVatAmount += Double.parseDouble(numericValue);
                                }
                            } catch (NumberFormatException e) {
                                // 忽略无法解析的金额
                            }
                        }

                        vatData.put("invoiceNum", vat.getString("invoiceNum"));
                        vatData.put("invoiceDate", vat.getString("invoiceDate"));
                        vatData.put("sellerName", vat.getString("sellerName"));

                        // 添加到结果集
                        vatReceipts.put(vatData);
                    }
                }
            }

            // 判断是否有发票 - 直接通过vatAttachIds判断
            boolean hasVatInvoice = vatAttachIds != null && !vatAttachIds.isEmpty();

            // 计算报销金额
            Map<String, Integer> standards = getTravelStandard(city, positionCode);
            double totalActual = totalTrainAmount + totalVatAmount;
            double totalExpenseAmount = 0.0;

            // 修改计算报销金额的逻辑
            if (standards != null) {
                // 从数据库获取的各项标准金额
                int accomStandard = standards.get("accommodationStandard");  // 住宿标准（元/晚）
                int livingStandard = standards.get("livingAllowance");       // 生活补贴标准（元/天）
                int transportStandard = standards.get("transportAllowance"); // 交通补贴标准（元/天）
                // 住宿晚数 = 出差天数 - 1
                int nights = Math.max(0, days - 1); // 确保不会出现负数
                // 住宿标准总额 - 仅当有发票时才计算，否则为0
                double accomStandardTotal = hasVatInvoice ? accomStandard * nights : 0;
                // 补贴总额 = 天数 × (生活补贴 + 交通补贴)
                double allowanceTotal = days * (livingStandard + transportStandard);
                // 实际报销的住宿费用 - 以较小值为准（实际费用或标准总额）
                double reimburseAccomAmount;
                if (!hasVatInvoice) {
                    // 无发票情况：住宿费用为0
                    reimburseAccomAmount = 0;
                    // 可报销金额 = 实际费用(主要是火车票) + 全部补贴
                    totalExpenseAmount = totalActual + allowanceTotal;
                } else if (totalVatAmount <= accomStandardTotal) {
                    // 有发票且未超支：以实际住宿费用为准
                    reimburseAccomAmount = totalVatAmount;
                    // 可报销金额 = 火车票 + 实际住宿费用 + 全部补贴
                    totalExpenseAmount = totalTrainAmount + totalVatAmount + allowanceTotal;
                } else {
                    // 超支情况（有发票且超支）
                    // 超出金额 = 实际住宿费用 - 住宿标准总额
                    double overBudget = totalVatAmount - accomStandardTotal;
                    // 超支比例 = 超出金额 / 住宿标准总额
                    double overBudgetRatio = overBudget / accomStandardTotal;
                    if (overBudgetRatio <= 0.2) {
                        // 超支20%以内：个人承担超额部分的50%
                        double personalExpense = overBudget * 0.5;  // 个人承担费用
                        // 可报销的住宿费用 = 实际住宿费用 - 个人承担部分
                        reimburseAccomAmount = totalVatAmount - personalExpense;
                    } else {
                        // 超支20%以上：住宿仅报销标准额度
                        reimburseAccomAmount = accomStandardTotal;
                    }
                    // 最终报销总额 = 火车票 + 可报销的住宿费用 + 全部补贴
                    totalExpenseAmount = totalTrainAmount + reimburseAccomAmount + allowanceTotal;
                }

                // 将标准信息添加到响应中，供前端展示
                response.put("standardInfo", new JSONObject()
                        .put("accommodationStandard", accomStandard)         // 住宿标准（元/晚）
                        .put("livingAllowance", livingStandard)              // 生活补贴标准（元/天）
                        .put("transportAllowance", transportStandard)        // 交通补贴标准（元/天）
                        .put("accomStandardTotal", accomStandardTotal)       // 住宿标准总额
                        .put("allowanceTotal", allowanceTotal)               // 补贴总额
                        .put("reimburseAccomAmount", reimburseAccomAmount)   // 可报销的住宿费用
                        .put("hasVatInvoice", hasVatInvoice)                 // 是否有发票
                        .put("days", days)                                   // 出差天数
                        .put("nights", nights)                               // 住宿晚数
                );
            } else {
                // 未找到差旅标准时，默认全额报销实际费用（不含补贴）
                totalExpenseAmount = totalActual;
            }

            // 构建最终响应
            response.put("success", true);
            response.put("trainTickets", trainTickets);
            response.put("vatReceipts", vatReceipts);
            response.put("totalTrainAmount", totalTrainAmount);
            response.put("totalVatAmount", totalVatAmount);
            response.put("totalActualAmount", totalActual);
            response.put("totalExpenseAmount", totalExpenseAmount); // 可报销金额
            response.put("destination", city);
            response.put("position", mapPositionToName(positionCode));

            return response;

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "处理失败: " + e.getMessage());
            response.put("stackTrace", e.toString());
            return response;
        }
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
     * 获取差旅标准
     * @param city 城市
     * @param positionCode 职位代码
     * @return 包含差旅标准的Map
     */
    private Map<String, Integer> getTravelStandard(String city, String positionCode) {
        if (city == null || city.isEmpty() || positionCode == null || positionCode.isEmpty()) {
            return null;
        }

        String positionPrefix = getPositionPrefix(positionCode);
        if (positionPrefix == null) {
            return null;
        }

        RecordSet rs = new RecordSet();
        Map<String, Integer> standards = null;

        try {
            // 先尝试精确匹配城市
            String sql = "SELECT " +
                    positionPrefix + "zsbx AS accom_standard, " +
                    positionPrefix + "shbz AS living_allowance, " +
                    positionPrefix + "jtbz AS transport_allowance " +
                    "FROM uf_travel_standards WHERE cs = ?";

            rs.executeQuery(sql, city);

            if (rs.next()) {
                standards = new HashMap<>();
                standards.put("accommodationStandard", rs.getInt("accom_standard"));
                standards.put("livingAllowance", rs.getInt("living_allowance"));
                standards.put("transportAllowance", rs.getInt("transport_allowance"));
                return standards;
            }

            // 尝试匹配省份
            sql = "SELECT sf FROM uf_travel_standards WHERE cs = ?";
            rs.executeQuery(sql, city);

            String province = null;
            if (rs.next()) {
                province = rs.getString("sf");
            }

            if (province != null && !province.isEmpty()) {
                sql = "SELECT " +
                        positionPrefix + "zsbx AS accom_standard, " +
                        positionPrefix + "shbz AS living_allowance, " +
                        positionPrefix + "jtbz AS transport_allowance " +
                        "FROM uf_travel_standards WHERE sf = ? AND (cs IS NULL OR cs = '')";

                rs.executeQuery(sql, province);

                if (rs.next()) {
                    standards = new HashMap<>();
                    standards.put("accommodationStandard", rs.getInt("accom_standard"));
                    standards.put("livingAllowance", rs.getInt("living_allowance"));
                    standards.put("transportAllowance", rs.getInt("transport_allowance"));
                    return standards;
                }
            }

            // 最后尝试获取默认标准
            sql = "SELECT " +
                    positionPrefix + "zsbx AS accom_standard, " +
                    positionPrefix + "shbz AS living_allowance, " +
                    positionPrefix + "jtbz AS transport_allowance " +
                    "FROM uf_travel_standards WHERE (sf IS NULL OR sf = '') AND (cs IS NULL OR cs = '') " +
                    "AND (qy IS NOT NULL AND qy != '') LIMIT 1";

            rs.execute(sql);

            if (rs.next()) {
                standards = new HashMap<>();
                standards.put("accommodationStandard", rs.getInt("accom_standard"));
                standards.put("livingAllowance", rs.getInt("living_allowance"));
                standards.put("transportAllowance", rs.getInt("transport_allowance"));
                return standards;
            }

        } catch (Exception e) {
            // 忽略异常
        }

        return null;
    }

    /**
     * 获取职位字段前缀
     */
    private String getPositionPrefix(String positionCode) {
        switch (positionCode) {
            case "0": return "ywy"; // 业务员
            case "1": return "bmfzr"; // 部门负责人
            case "2": return "fz"; // 副总
            case "3": return "zjl"; // 总经理
            case "4": return "gdy"; // 跟单员
            default: return null;
        }
    }

    /**
     * 根据职位代码获取职位名称
     */
    private String mapPositionToName(String positionCode) {
        switch (positionCode) {
            case "0": return "业务员";
            case "1": return "部门负责人";
            case "2": return "副总";
            case "3": return "总经理";
            case "4": return "跟单员";
            default: return "未知职位";
        }
    }
}