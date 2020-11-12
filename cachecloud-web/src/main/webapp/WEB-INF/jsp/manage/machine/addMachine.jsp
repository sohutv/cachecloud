<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<div id="addMachineModal${machine.info.id}" class="modal fade" tabindex="-1" data-width="400">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
				<h4 class="modal-title">管理机器</h4>
			</div>
			
			<form class="form-horizontal form-bordered form-row-stripped">
				<div class="modal-body">
					<div class="row">
						<!-- 控件开始 -->
						<div class="col-md-12">
							<!-- form-body开始 -->
							<div class="form-body">
								<div class="form-group">
									<label class="control-label col-md-3">
										机器ip:
									</label>
									<div class="col-md-5">
										<input type="text" name="ip" id="ip${machine.info.id}"
											value="${machine.info.ip}" placeholder="机器ip，多台机器,分隔"
											class="form-control" />
									</div>
                                    <label class="control-label col-md-4">
                                        注:多台机器用,分隔
									</label>
								</div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        机房:
                                    </label>
                                    <div class="col-md-5">
                                        <select name="machineoom" id="machineRoom${machine.info.id}" class="form-control select2_category">
                                            <option value="默认">默认机房</option>
                                            <c:forEach items="${roomList}" var="room">
                                                <option value="${room.name}" <c:if test="${room.name == machine.info.room}">selected</c:if>>${room.name} (${room.ipNetwork})</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        内存:
                                    </label>
                                    <div class="col-md-5">
                                        <input type="text" name="mem" id="mem${machine.info.id}"
                                               value="${machine.info.mem}" placeholder="机器内存（单位G）"
                                               class="form-control" />
                                    </div>
                                    <label class="control-label">
                                        G
									</label>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        cpu:
                                    </label>
                                    <div class="col-md-5">
                                        <input type="text" name="cpu" id="cpu${machine.info.id}"
                                               value="${machine.info.cpu}" placeholder="机器CPU核数"
                                               class="form-control" />
                                    </div>
                                    <label class="control-label">
                                        核
									</label>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        disk:
                                    </label>
                                    <div class="col-md-5">
                                        <input type="text" name="disk" id="disk${machine.info.id}"
                                               value="${machine.info.disk}" placeholder="机器磁盘空间:G"
                                               class="form-control" />
                                    </div>
                                    <label class="control-label">
                                        G
                                    </label>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        是否虚机:
                                    </label>
                                    <div class="col-md-5">
                                        <select name="virtual" id="virtual${machine.info.id}" class="form-control select2_category">
                                            <option value="0" <c:if test="${machine.info.virtual == 0}">selected="selected"</c:if>>
                                                否
                                            </option>
                                            <option value="1" <c:if test="${machine.info.virtual == 1}">selected="selected"</c:if>>
                                                是
                                            </option>
                                        </select>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        宿主机ip:
                                    </label>
                                    <div class="col-md-5">
                                        <input type="text" name="realIp" id="realIp${machine.info.id}"
                                               value="${machine.info.realIp}" placeholder="宿主机ip,多台机器,分隔"
                                               class="form-control" />
                                    </div>
                                    <label class="control-label col-md-4">
                                        注:多台机器用,分隔
									</label>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                      	  机架信息:
                                    </label>
                                    <div class="col-md-5">
                                        <input type="text" name="rack" id="rack${machine.info.id}"
                                               value="${machine.info.rack}" placeholder="机器信息"
                                               class="form-control" />
                                    </div>
                                </div>
                                
                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        	机器类型:
                                    </label>
                                    <div class="col-md-5">
                                        <select name="machineType" id="machineType${machine.info.id}" class="form-control select2_category">
                                            <option value="0" <c:if test="${machine.info.type == 0}">selected="selected"</c:if>>
                                                Redis机器(默认)
                                            </option>
                                             <option value="3" <c:if test="${machine.info.type == 3}">selected="selected"</c:if>>
                                                Sentinel机器
                                            </option>
                                             <option value="4" <c:if test="${machine.info.type == 4}">selected="selected"</c:if>>
                                                Twemproxy机器
                                            </option>
                                             <option value="5" <c:if test="${machine.info.type == 5}">selected="selected"</c:if>>
                                                Pika机器
                                            </option>
                                            <option value="2" <c:if test="${machine.info.type == 2}">selected="selected"</c:if>>
                                                Redis迁移工具机器
                                            </option>
                                        </select>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        部署类型:
                                    </label>
                                    <div class="col-md-5">
                                        <select name="useType" id="useType1${machine.info.id}" class="form-control select2_category">
                                            <option value="2" <c:if test="${machine.info.useType == 2}">selected="selected"</c:if>>
                                                混合部署
                                            </option>
                                            <option value="0" <c:if test="${machine.info.useType == 0}">selected="selected"</c:if>>
                                                专用服务部署
                                            </option>
                                            <option value="1" <c:if test="${machine.info.useType == 1}">selected="selected"</c:if>>
                                                测试服务部署
                                            </option>
                                            <%--<option value="3" <c:if test="${machine.info.useType == 3}">selected="selected"</c:if>>--%>
                                                <%--Sentinel部署--%>
                                            <%--</option>--%>
                                        </select>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        容器类型:
                                    </label>
                                    <div class="col-md-5">
                                        <select name="k8sType" id="k8sType${machine.info.id}" class="form-control select2_category">
                                            <option value="0" <c:if test="${machine.info.k8sType == 0}">selected="selected"</c:if>>
                                                普通容器
                                            </option>
                                            <option value="1" <c:if test="${machine.info.k8sType == 1}"> selected="selected"</c:if>>
                                                k8s容器
                                            </option>
                                            <option value="2" <c:if test="${machine.info.k8sType == 2}"> selected="selected"</c:if>>
                                                物理机
                                            </option>
                                            <option value="3" <c:if test="${machine.info.k8sType == 3}"> selected="selected"</c:if>>
                                                虚拟机
                                            </option>
                                        </select>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                      	  机器说明:
                                    </label>
                                    <div class="col-md-5">
                                        <input type="text" name="extraDesc" id="extraDesc${machine.info.id}"
                                               value="${machine.info.extraDesc}" placeholder="描述说明(所属服务、相关人员)"
                                               class="form-control" />
                                    </div>
                                </div>
                                
                               <div class="form-group">
                                    <label class="control-label col-md-3">
                                        状态收集:
                                    </label>
                                    <div class="col-md-5">
                                        <select name="collect" id="collect${machine.info.id}" class="form-control select2_category">
                                            <option value="0" <c:if test="${machine.info.collect == 0}">selected="selected"</c:if>>
                                                关闭
                                            </option>
                                            <option value="1" <c:if test="${machine.info.collect == 1 || empty machine.info.id}">selected="selected"</c:if>>
                                                开启
                                            </option>
                                        </select>
                                    </div>
                                </div>



								<input type="hidden" id="machineId${machine.info.id}" name="machineId" value="${machine.info.id}"/>
								<input type="hidden" id="versionInfo${machine.info.id}" name="versionInfo" value="${machine.info.versionInstall}"/>
							</div>
							<!-- form-body 结束 -->
						</div>
						<div id="machineInfo${machine.info.id}"></div>
						<!-- 控件结束 -->
					</div>
				</div>
				
				<div class="modal-footer">
					<button type="button" data-dismiss="modal" class="btn" >Close</button>
					<button type="button" id="addMachineBtn${machine.info.id}" class="btn red" onclick="saveOrUpdateMachine('${machine.info.id}')">Ok</button>
				</div>
			
			</form>
		</div>
	</div>
</div>

