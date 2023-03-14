INSERT INTO devtools_db.project
(id, project_name, project_type, project_desc, owner_user_id, create_user_id, update_user_id)
VALUES(1, 'testProject', 1, '这是测试数据', 'testUser', 'testUser', 'testUser');

INSERT INTO devtools_db.dubbo_service
(id, project_id, service_name, simple_service_name, service_path, registry_zk_cluster, provider_group, provider_version, timeout, owner_user_id, create_user_id, update_user_id)
VALUES(1, 1, 'testService', 'testService', 'com.xchen.heimdall.dubbo.api.test.project', 1, '', '', 0, 'testUser', 'testUser', 'testUser');

INSERT INTO devtools_db.dubbo_method
(id, service_id, method_name, vo_id, vo_wrapper_type, dto_id, dto_wrapper_type, api_desc, api_remark, code_status, owner_user_id, create_user_id, update_user_id, method_exception)
VALUES(1, 1, 'testMethod1', 1, 1, 3, 1, 'method1', '', 0, 'testUser', 'testUser', 'testUser', 'CUSTOM');
INSERT INTO devtools_db.dubbo_method
(id, service_id, method_name, vo_id, vo_wrapper_type, dto_id, dto_wrapper_type, api_desc, api_remark, code_status, owner_user_id, create_user_id, update_user_id)
VALUES(2, 1, 'testMethod2', 2, 1, 3, 1, 'method2', '', 0, 'testUser', 'testUser', 'testUser');
INSERT INTO devtools_db.dubbo_method
(id, service_id, method_name, vo_id, vo_wrapper_type, dto_id, dto_wrapper_type, api_desc, api_remark, code_status, owner_user_id, create_user_id, update_user_id)
VALUES(3, 1, 'testMethod3', 1, 1, 4, 1, 'method3', '', 1, 'testUser', 'testUser', 'testUser');

INSERT INTO devtools_db.pojo
(id, project_id, pojo_name, pojo_type, pojo_desc, pojo_path, parent_id, field_list, owner_user_id, create_user_id, update_user_id)
VALUES(1, 1, 'testVO1', 0, 'testVO1', 'com.xchen.heimdall.dubbo.api.test.project.vo', NULL, '[{"id":111,"fieldName":"testField","ownerUserId":"testUser","logExcluded":true,"notNull":true,"fieldDesc":"desc","example":"ssss","fieldType":"String"}]', 'testUser', 'testUser', 'testUser');
INSERT INTO devtools_db.pojo
(id, project_id, pojo_name, pojo_type, pojo_desc, pojo_path, parent_id, field_list, owner_user_id, create_user_id, update_user_id)
VALUES(2, 1, 'testVO2', 0, 'testVO2', 'com.xchen.heimdall.dubbo.api.test.project.vo', 1, '[{"id":111,"fieldName":"testField","ownerUserId":"testUser","logExcluded":true,"notNull":true,"fieldDesc":"desc","example":"ssss","fieldType":"testVO1"}]', 'testUser', 'testUser', 'testUser');
INSERT INTO devtools_db.pojo
(id, project_id, pojo_name, pojo_type, pojo_desc, pojo_path, parent_id, field_list, owner_user_id, create_user_id, update_user_id)
VALUES(3, 1, 'testDTO1', 1, 'testDTO1', 'com.xchen.heimdall.dubbo.api.test.project.dto', NULL, '[{"id":111,"fieldName":"testField","ownerUserId":"testUser","logExcluded":true,"notNull":true,"fieldDesc":"desc","example":"ssss","fieldType":"Map<String, testVO1>"}]', 'testUser', 'testUser', 'testUser');
INSERT INTO devtools_db.pojo
(id, project_id, pojo_name, pojo_type, pojo_desc, pojo_path, parent_id, field_list, owner_user_id, create_user_id, update_user_id)
VALUES(4, 1, 'testDTO2', 1, 'testDTO2', 'com.xchen.heimdall.dubbo.api.test.project.dto', NULL, '[{"id":111,"fieldName":"testField","ownerUserId":"testUser","logExcluded":true,"notNull":true,"fieldDesc":"desc","example":"ssss","fieldType":"String"}]', 'testUser', 'testUser', 'testUser');

INSERT INTO devtools_db.gateway_api
(id, service_id, method_id, login_required, permission_key_list, access_point_list, upstream_channel_type, owner_user_id, create_user_id, update_user_id)
VALUES(1, 1, 1, 1, '["sss","sss"]', '["INTRANET"]', 1, 'testUser', 'testUser', 'testUser');
INSERT INTO devtools_db.gateway_api
(id, service_id, method_id, login_required, permission_key_list, access_point_list, upstream_channel_type, owner_user_id, create_user_id, update_user_id)
VALUES(2, 2, 1, 1, '["sss","sss"]', '["INTRANET","INTERNET"]', 1,'testUser', 'testUser', 'testUser');

