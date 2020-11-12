<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<div id="addRoomModal${record.id}" class="modal fade" tabindex="-1" data-width="400">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                <h4 class="modal-title">管理机房</h4>
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
                                        机房名称:
                                    </label>
                                    <div class="col-md-5">
                                        <input type="text" name="name" id="name${record.id}"
                                               value="${record.name}" placeholder="机房名称"
                                               class="form-control" />
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        机房状态:
                                    </label>
                                    <div class="col-md-5">
                                        <select name="status" id="status${record.id}" class="form-control select2_category">
                                            <option value="${record.status}" <c:if test="${record.status == 1}">selected</c:if>>有效</option>
                                            <option value="${record.status}" <c:if test="${record.status == 0}">selected</c:if>>无效</option>
                                        </select>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        描述:
                                    </label>
                                    <div class="col-md-5">
                                        <input type="text" name="desc" id="desc${record.id}"
                                               value="${record.desc}" placeholder="描述"
                                               class="form-control" />
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        网段:
                                    </label>
                                    <div class="col-md-5">
                                        <input type="text" name="ipNetwork" id="ipNetwork${record.id}"
                                               value="${record.ipNetwork}" placeholder="网段示例:xx.xx.*.*"
                                               class="form-control" />
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        运营商:
                                    </label>
                                    <div class="col-md-5">
                                        <input type="text" name="operator" id="operator${record.id}"
                                               value="${record.operator}" placeholder="运营商"
                                               class="form-control" />
                                    </div>
                                </div>

                                <input type="hidden" id="roomId${record.id}" name="roomId" value="${record.id}"/>
                            </div>
                            <div id="machineRoom${record.id}"></div>
                            <!-- form-body 结束 -->
                        </div>
                        <!-- 控件结束 -->
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" data-dismiss="modal" class="btn" >Close</button>
                    <button type="button" id="addRoomBtn${record.id}" class="btn red" onclick="saveOrUpdateRoom('${record.id}')">Ok</button>
                </div>

            </form>
        </div>
    </div>
</div>
