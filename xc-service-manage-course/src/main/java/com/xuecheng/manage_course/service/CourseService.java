package com.xuecheng.manage_course.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.domain.course.response.DeleteCourseResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.*;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springfox.documentation.spring.web.json.Json;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 课程管理业务层
 *
 */
@Service
@Transactional
public class CourseService {
    @Autowired
    CourseBaseRepository courseBaseRepository;

    @Autowired
    CourseMapper courseMapper;

    @Autowired
    CourseMarketRepository courseMarketRepository;

    @Autowired
    CoursePicRepository coursePicRepository;

    @Autowired
    TeachplanService teachplanService;

    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    TeachplanRepository teachplanRepository;

    @Autowired
    TeachplanMediaRepository teachplanMediaRepository;

    @Autowired
    TeachplanMediaPubRepository teachplanMediaPubRepository;

    @Autowired
    CoursePubRepository coursePubRepository;

    @Autowired
    CmsPageClient cmsPageClient;

    @Value("${course-publish.siteId}")
    private String publish_siteId;
    @Value("${course-publish.templateId}")
    private String publish_templateId;
    @Value("${course-publish.previewUrl}")
    private String publish_previewUrl;
    @Value("${course-publish.pageWebPath}")
    private String publish_pageWebPath;
    @Value("${course-publish.pagePhysicalPath}")
    private String publish_pagePhysicalPath;
    @Value("${course-publish.dataUrlPre}")
    private String publish_dataUrlPre;


    //分页查询
    public QueryResponseResult<CourseInfo> findAll(int page, int size, CourseListRequest courseListRequest) {
        //将公司id传入dao
        if(courseListRequest==null){
            courseListRequest = new CourseListRequest();
        }

        PageHelper.startPage(page,size);
        //调用dao
        Page<CourseInfo> courseList = courseMapper.findCourseListPage(courseListRequest);
        List<CourseInfo> result = courseList.getResult();
        long total = courseList.getTotal();
        QueryResult<CourseInfo> queryResult = new QueryResult<>();
        queryResult.setList(result);
        queryResult.setTotal(total);
        return new QueryResponseResult<>(CommonCode.SUCCESS,queryResult);
    }


    //根据id查询课程基本信息
    public CourseBase getCourseBaseById(String courseId) {
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if(optional.isPresent()){
            return optional.get();
        }
        ExceptionCast.cast(CourseCode.COURSE_ISNULL);
        return null;
    }

    //根据id更新课程
    public ResponseResult updateCourseBase(String id, CourseBase courseBase) {
        CourseBase target = this.getCourseBaseById(id);
        if(target==null){
            ExceptionCast.cast(CourseCode.COURSE_PUBLISH_COURSEIDISNULL);
        }
        if(courseBase==null){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        target.setName(courseBase.getName());
        target.setMt(courseBase.getMt());
        target.setSt(courseBase.getSt());
        target.setUsers(courseBase.getUsers());
        target.setGrade(courseBase.getGrade());
        target.setStudymodel(courseBase.getStudymodel());
        target.setDescription(courseBase.getDescription());
        courseBaseRepository.save(target);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //新增课程
    public AddCourseResult addCourseBase(CourseBase courseBase) {
        if(courseBase==null){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        courseBaseRepository.save(courseBase);
        return new AddCourseResult(CommonCode.SUCCESS,courseBase.getId());
    }

    //获取课程营销信息
    public CourseMarket getCourseMarketById(String courseId) {
        Optional<CourseMarket> optional = courseMarketRepository.findById(courseId);
        if(!optional.isPresent()){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        return optional.get();
    }

    //更新课程营销信息
    public ResponseResult updateCourseMarket(String id, CourseMarket courseMarket) {
        CourseMarket target = this.getCourseMarketById(id);
        if(target==null){
            ExceptionCast.cast(CourseCode.COURSE_ISNULL);
        }
        target.setCharge(courseMarket.getCharge());
        target.setQq(courseMarket.getQq());
        target.setPrice(courseMarket.getPrice());
        target.setPrice_old(courseMarket.getPrice_old());
        target.setValid(courseMarket.getValid());
        target.setStartTime(courseMarket.getStartTime());
        target.setEndTime(courseMarket.getEndTime());
        courseMarketRepository.save(courseMarket);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //添加图片与课程的关联关系
    public ResponseResult addCoursePic(String courseId, String pic) {
        //课程图片信息
        CoursePic coursePic = null;
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        //判断courseId对应的CoursePic是否存在    存在的话就更新，否则就new一个
        if(optional.isPresent()){
            coursePic = optional.get();
        }
        if(coursePic==null){
            coursePic = new CoursePic();
        }
        coursePic.setCourseid(courseId);
        coursePic.setPic(pic);
        coursePicRepository.save(coursePic);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //查询课程图片
    public CoursePic findCoursePicList(String courseId) {
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        if(!optional.isPresent()){
            ExceptionCast.cast(CourseCode.COURSE_ISNULL);
        }
        return optional.get();
    }

    //删除课程图片
    public ResponseResult deleteCoursePicList(String courseId) {
        long l = coursePicRepository.deleteByCourseid(courseId);
        if(l>0){
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    //查询课程的视图，包括基本信息、图片、营销、课程计划
    public CourseView getCourseView(String courseId) {
        CourseView courseView = new CourseView();

        //获取课程的基本信息
        CourseBase courseBase = this.getCourseBaseById(courseId);
        if(courseBase!=null){
            courseView.setCourseBase(courseBase);
        }

        //获取课程的图片
        CoursePic coursePicList = this.findCoursePicList(courseId);
        if(coursePicList!=null){
            courseView.setCoursePic(coursePicList);
        }

        //获取课程营销信息
        CourseMarket courseMarket = this.getCourseMarketById(courseId);
        if(courseMarket!=null){
            courseView.setCourseMarket(courseMarket);
        }

        //获取课程计划
        TeachplanNode teachPlanList = teachplanService.findTeachPlanList(courseId);
        if(teachPlanList!=null){
            courseView.setTeachplanNode(teachPlanList);
        }

        return courseView;
    }

    //课程预览
    public CoursePublishResult preview(String courseId) {
        CourseBase courseBase = this.getCourseBaseById(courseId);
        //请求cms添加页面
        //准备cmsPage的信息
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);  //站点id
        cmsPage.setTemplateId(publish_templateId);  //模板id
        cmsPage.setPageWebPath(publish_pageWebPath);   //页面网络路径
        cmsPage.setPagePhysicalPath(publish_pagePhysicalPath);  //页面物理路径
        cmsPage.setDataUrl(publish_dataUrlPre+courseId);    //数据模型url
        cmsPage.setPageName(courseId+".html");  //页面名称
        cmsPage.setPageAliase(courseBase.getName());    //页面别名，课程名称
        //远程调用cms
        CmsPageResult cmsPageResult = cmsPageClient.saveCmsPage(cmsPage);
        if(!cmsPageResult.isSuccess()){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        CmsPage cmsPage1 = cmsPageResult.getCmsPage();
        String pageId = cmsPage1.getPageId();   //获取页面id
        //拼装页面的预览url
        String previewUrl = publish_previewUrl+pageId;
        //返回CoursePublishResult(当中包含了页面预览的url)
        return new CoursePublishResult(CommonCode.SUCCESS,previewUrl);
    }

    //课程发布
    public CoursePublishResult publish(String courseId) {
        //准备课程信息
        CourseBase courseBase = this.getCourseBaseById(courseId);
        //准备页面信息
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);  //站点id
        cmsPage.setTemplateId(publish_templateId);  //模板id
        cmsPage.setPageWebPath(publish_pageWebPath);   //页面网络路径
        cmsPage.setPagePhysicalPath(publish_pagePhysicalPath);  //页面物理路径
        cmsPage.setDataUrl(publish_dataUrlPre+courseId);    //数据模型url
        cmsPage.setPageName(courseId+".html");  //页面名称
        cmsPage.setPageAliase(courseBase.getName());    //页面别名，课程名称

        //调用cms一键发布接口将详情页面发布到服务器
        CmsPostPageResult cmsPostPageResult = cmsPageClient.postPageQuick(cmsPage);
        if(!cmsPostPageResult.isSuccess()){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        //保存课程的发布状态为“已发布”
        //更新课程发布状态为已发布 202002
        courseBase.setStatus("202002");
        courseBaseRepository.save(courseBase);
        //保存课程索引信息
        //先创建一个CoursePub对象
        CoursePub coursePub = this.createCoursePub(courseId);
        //将coursePub对象把存到数据库
        this.saveCoursePub(courseId,coursePub);
        //缓存课程信息

        //得到页面url
        String previewUrl=cmsPostPageResult.getPageUrl();
        //向teachplanMediaPub中保存媒资信息
        this.saveTeachPlanMediaPub(courseId);
        return new CoursePublishResult(CommonCode.SUCCESS,previewUrl);
    }

    //向teachplanMediaPub中保存媒资信息
    private void saveTeachPlanMediaPub(String courseId){
        //先删除teachplanMediaPub中的信息
        teachplanMediaPubRepository.deleteByCourseId(courseId);
        //从teachplanMedia中查询
        List<TeachplanMedia> teachplanMediaList = teachplanMediaRepository.findByCourseId(courseId);
        //将list数据插入到pub表中
        List<TeachplanMediaPub> teachplanMediaPubList = new ArrayList<>();
        //将teachplanMediaList的数据放到teachplanMediaPubList中
        for(TeachplanMedia teachplanMedia:teachplanMediaList){
            TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
            BeanUtils.copyProperties(teachplanMedia,teachplanMediaPub);
            //添加时间戳
            teachplanMediaPub.setTimestamp(new Date());
            teachplanMediaPubList.add(teachplanMediaPub);
        }
        teachplanMediaPubRepository.saveAll(teachplanMediaPubList);
    }

    //将coursePub对象把存到数据库
    private CoursePub saveCoursePub(String id,CoursePub coursePub){
        CoursePub coursePubNew = null;
        //根据课程id查询coursePub
        //coursePubNew = coursePubOptional.orElseGet(CoursePub::new);
        Optional<CoursePub> coursePubOptional = coursePubRepository.findById(id);
        if(coursePubOptional.isPresent()){
            coursePubNew = coursePubOptional.get();
        }else{
            coursePubNew = new CoursePub();
        }

        //将coursePub对象中的信息保存到coursePubNew中
        BeanUtils.copyProperties(coursePub,coursePubNew);
        coursePubNew.setId(id);
        //时间戳,给logstach使用
        coursePubNew.setTimestamp(new Date());
        //发布时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = simpleDateFormat.format(new Date());
        coursePubNew.setPubTime(date);
        coursePubRepository.save(coursePubNew);
        return coursePubNew;
    }

    //创建coursePub对象
    private CoursePub createCoursePub(String courseId){
        CoursePub coursePub = new CoursePub();
        //根据课程id查询course_base
        CourseBase courseBase = this.getCourseBaseById(courseId);
        //将courseBase属性拷贝到coursePub中
        BeanUtils.copyProperties(courseBase,coursePub);

        //根据课程id查询course_pic
        CoursePic coursePic = this.findCoursePicList(courseId);
        //将coursePic属性拷贝到coursePub中
        BeanUtils.copyProperties(coursePic,coursePub);

        //根据课程id查询course_Market
        CourseMarket courseMarket = this.getCourseMarketById(courseId);
        //将coursePic属性拷贝到courseMarket中
        BeanUtils.copyProperties(courseMarket,coursePub);

        //课程计划信息
        TeachplanNode teachplanNode =teachplanMapper.selectList(courseId);
        //将课程计划信息json串保存到coursePub
        String jsonString = JSON.toJSONString(teachplanNode);
        coursePub.setTeachplan(jsonString);
        return coursePub;
    }

    //保存课程计划与媒资文件的关联
    public ResponseResult savemedia(TeachplanMedia teachplanMedia) {
        if(teachplanMedia==null || StringUtils.isEmpty(teachplanMedia.getTeachplanId())){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }

        String teachplanId = teachplanMedia.getTeachplanId();
        //查询课程计划
        Optional<Teachplan> optional = teachplanRepository.findById(teachplanId);
        if(!optional.isPresent()){
            ExceptionCast.cast(CourseCode.COURSE_TEACHPLANISNULL);
        }
        Teachplan teachplan = optional.get();
        String grade = teachplan.getGrade();
        //校验课程计划是不是三级
        if(StringUtils.isEmpty(grade)||!grade.equals("3")){
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_GRADEERROR);
        }

        TeachplanMedia one = null;
        Optional<TeachplanMedia> mediaOptional = teachplanMediaRepository.findById(teachplanId);
        if(mediaOptional.isPresent()){
            one = mediaOptional.get();
        }else {
            one = new TeachplanMedia();
        }
        //将teachplanMedia保存到数据库
        one.setCourseId(teachplan.getCourseid());
        one.setMediaId(teachplanMedia.getMediaId());
        one.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());
        one.setMediaUrl(teachplanMedia.getMediaUrl());
        one.setTeachplanId(teachplanId);
        teachplanMediaRepository.save(one);
        return new ResponseResult(CommonCode.SUCCESS);
    }
}
