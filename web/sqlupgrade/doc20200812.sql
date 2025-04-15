--oracle
create table cus_classified(
  classificationLevel varchar2(999),
  drafterName varchar2(999),
  issuerName varchar2(999),
  ClassifiedOrganizationName varchar2(999),
  classifiedTimeNum varchar2(999),
  classifiedTimeUnit varchar2(999),
  docid varchar2(999),
  requestid varchar2(999),
);

--sqlserver

create table cus_classified
(
classificationLevel nvarchar(max),--密级等级，1，绝密（最高）2，机密（次高）3，秘密（再次）4，内部（次低）5，公开（最低） (必填项)
drafterName nvarchar(max),--  文件起草人姓名 (必填项)
issuerName nvarchar(max),--文件签发人姓名
ClassifiedOrganizationName nvarchar(max),-- 定密单位名称，多个单位名称用;连接 (必填项)
classifiedTimeNum nvarchar(max),-- 定密时间 数值
classifiedTimeUnit nvarchar(max),-- 定密时间 单位
docid nvarchar(max),--文档ID
requestid  nvarchar(max)--流程ID
)