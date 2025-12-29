
package com.typesafe.config.impl

import com.typesafe.config._
import io.github.dkichler.config.ConfigRecordException.BadRecord
import io.github.dkichler.config.{ConfigRecordException, ConfigRecordFactory}
import org.junit.Assert._
import org.junit._
import recordconfig.EnumsConfig.{Problem, Solution}
import recordconfig._

import java.io.{InputStream, InputStreamReader}
import java.time.Duration
import scala.collection.Seq
import scala.jdk.CollectionConverters.{IterableHasAsScala, ListHasAsScala, MapHasAsJava, SeqHasAsJava, SetHasAsJava, SetHasAsScala}
import scala.reflect.{ClassTag, classTag}

/**
 * This test class is defined in com.typesafe.config.impl in order to access some of the internals of
 * the original library to ensure valid tests
 */
class ConfigRecordFactoryTest {

    @Test
    def testCreate() {
        val configIs: InputStream = this.getClass().getClassLoader().getResourceAsStream("recordconfig/recordconfig01.conf")
        val config: Config = ConfigFactory.parseReader(new InputStreamReader(configIs),
            ConfigParseOptions.defaults.setSyntax(ConfigSyntax.CONF)).resolve
        val recordConfig: TestRecordConfig = ConfigRecordFactory.create(config, classOf[TestRecordConfig])
        assertNotNull(recordConfig)
        // recursive record inside the first record
        assertEquals(3, recordConfig.numbers.intVal)
    }

    @Test
    def testValidation() {
        val configIs: InputStream = this.getClass().getClassLoader().getResourceAsStream("recordconfig/recordconfig01.conf")
        val config: Config = ConfigFactory.parseReader(new InputStreamReader(configIs),
            ConfigParseOptions.defaults.setSyntax(ConfigSyntax.CONF)).resolve.getConfig("validation")
        val e = intercept[ConfigException.ValidationFailed] {
            ConfigRecordFactory.create(config, classOf[ValidationRecordConfig])
        }

        val expecteds = Seq(Missing("numbers", 101, "recordconfig.NumbersConfig"),
            Missing("propNotListedInConfig", 101, "string"),
            WrongType("shouldBeInt", 102, "number", "boolean"),
            WrongType("should-be-boolean", 103, "boolean", "number"),
            WrongType("should-be-list", 104, "list", "string"))

        checkValidationException(e, expecteds)
    }

    @Test
    def testUnresolvedConfig(): Unit = {
        val e = intercept[ConfigException.NotResolved] {
            val conf =
                """
                  |abcd = ${unresolved}
                  |yes = "yes"
                  |""".stripMargin
            ConfigRecordFactory.create(parseConfig(conf), classOf[StringsConfig])
        }
        assertTrue("unresolved substitution error", e.getMessage.contains("need to Config#resolve() a config before using it"))
    }

    @Test
    def testCreateBool() {
        val recordConfig: BooleansConfig = ConfigRecordFactory.create(loadConfig().getConfig("booleans"), classOf[BooleansConfig])
        assertNotNull(recordConfig)
        assertEquals(true, recordConfig.trueVal)
        assertEquals(false, recordConfig.falseVal)
    }

    @Test
    def testCreateString() {
        val recordConfig: StringsConfig = ConfigRecordFactory.create(loadConfig().getConfig("strings"), classOf[StringsConfig])
        assertNotNull(recordConfig)
        assertEquals("abcd", recordConfig.abcd)
        assertEquals("yes", recordConfig.yes)
    }

    @Test
    def testCreateEnum() {
        val recordConfig: EnumsConfig = ConfigRecordFactory.create(loadConfig().getConfig("enums"), classOf[EnumsConfig])
        assertNotNull(recordConfig)
        assertEquals(Problem.P1, recordConfig.problem)
        assertEquals(List(Solution.S1, Solution.S3), recordConfig.solutions.asScala)
    }

    @Test
    def testCreateNumber() {
        val recordConfig: NumbersConfig = ConfigRecordFactory.create(loadConfig().getConfig("numbers"), classOf[NumbersConfig])
        assertNotNull(recordConfig)

        assertEquals(3, recordConfig.intVal)
        assertEquals(3, recordConfig.intObj)

        assertEquals(4L, recordConfig.longVal)
        assertEquals(4L, recordConfig.longObj)

        assertEquals(1.0, recordConfig.doubleVal, 1e-6)
        assertEquals(1.0, recordConfig.doubleObj, 1e-6)
    }

    @Test
    def testCreateList() {
        val recordConfig: ArraysConfig = ConfigRecordFactory.create(loadConfig().getConfig("arrays"), classOf[ArraysConfig])
        assertNotNull(recordConfig)
        assertEquals(List().asJava, recordConfig.empty)
        assertEquals(List(1, 2, 3).asJava, recordConfig.ofInt)
        assertEquals(List(32L, 42L, 52L).asJava, recordConfig.ofLong)
        assertEquals(List("a", "b", "c").asJava, recordConfig.ofString)
        assertEquals(3, recordConfig.ofObject.size)
        assertEquals(3, recordConfig.ofDouble.size)
        assertEquals(3, recordConfig.ofConfig.size)
        assertTrue(recordConfig.ofConfig.get(0).isInstanceOf[Config])
        assertEquals(3, recordConfig.ofConfigObject.size)
        assertTrue(recordConfig.ofConfigObject.get(0).isInstanceOf[ConfigObject])
        assertEquals(List(intValue(1), intValue(2), stringValue("a")),
            recordConfig.ofConfigValue.asScala)
        assertEquals(List(Duration.ofMillis(1), Duration.ofHours(2), Duration.ofDays(3)),
            recordConfig.ofDuration.asScala)
        assertEquals(List(ConfigMemorySize.ofBytes(1024),
            ConfigMemorySize.ofBytes(1048576),
            ConfigMemorySize.ofBytes(1073741824)),
            recordConfig.ofMemorySize.asScala)

        val stringsConfigOne = new StringsConfig("testAbcdOne", "testYesOne")
        val stringsConfigTwo = new StringsConfig("testAbcdTwo", "testYesTwo")

        assertEquals(List(stringsConfigOne, stringsConfigTwo).asJava, recordConfig.ofStringRecord)
    }

    @Test
    def testCreateSet() {
        val recordConfig: SetsConfig = ConfigRecordFactory.create(loadConfig().getConfig("sets"), classOf[SetsConfig])
        assertNotNull(recordConfig)
        assertEquals(Set().asJava, recordConfig.empty)
        assertEquals(Set(1, 2, 3).asJava, recordConfig.ofInt)
        assertEquals(Set(32L, 42L, 52L).asJava, recordConfig.ofLong)
        assertEquals(Set("a", "b", "c").asJava, recordConfig.ofString)
        assertEquals(3, recordConfig.ofObject.size)
        assertEquals(3, recordConfig.ofDouble.size)
        assertEquals(3, recordConfig.ofConfig.size)
        assertTrue(recordConfig.ofConfig.iterator().next().isInstanceOf[Config])
        assertEquals(3, recordConfig.ofConfigObject.size)
        assertTrue(recordConfig.ofConfigObject.iterator().next().isInstanceOf[ConfigObject])
        assertEquals(Set(intValue(1), intValue(2), stringValue("a")),
            recordConfig.ofConfigValue.asScala)
        assertEquals(Set(Duration.ofMillis(1), Duration.ofHours(2), Duration.ofDays(3)),
            recordConfig.ofDuration.asScala)
        assertEquals(Set(ConfigMemorySize.ofBytes(1024),
            ConfigMemorySize.ofBytes(1048576),
            ConfigMemorySize.ofBytes(1073741824)),
            recordConfig.ofMemorySize.asScala)

        val stringsConfigOne = new StringsConfig("testAbcdOne", "testYesOne")
        val stringsConfigTwo = new StringsConfig("testAbcdTwo", "testYesTwo")

        assertEquals(Set(stringsConfigOne, stringsConfigTwo).asJava, recordConfig.ofStringRecord)
    }

    @Test
    def testCreateDuration() {
        val recordConfig: DurationsConfig = ConfigRecordFactory.create(loadConfig().getConfig("durations"), classOf[DurationsConfig])
        assertNotNull(recordConfig)
        assertEquals(Duration.ofMillis(500), recordConfig.halfSecond)
        assertEquals(Duration.ofMillis(1000), recordConfig.second)
        assertEquals(Duration.ofMillis(1000), recordConfig.secondAsNumber)
    }

    @Test
    def testCreateBytes() {
        val recordConfig: BytesConfig = ConfigRecordFactory.create(loadConfig().getConfig("bytes"), classOf[BytesConfig])
        assertNotNull(recordConfig)
        assertEquals(ConfigMemorySize.ofBytes(1024), recordConfig.kibibyte)
        assertEquals(ConfigMemorySize.ofBytes(1000), recordConfig.kilobyte)
        assertEquals(ConfigMemorySize.ofBytes(1000), recordConfig.thousandBytes)
    }

    @Test
    def testPreferCamelNames() {
        val recordConfig = ConfigRecordFactory.create(loadConfig().getConfig("preferCamelNames"), classOf[PreferCamelNamesConfig])
        assertNotNull(recordConfig)

        assertEquals("yes", recordConfig.fooBar)
        assertEquals("yes", recordConfig.bazBar)
    }

    @Test
    def testValues() {
        val recordConfig = ConfigRecordFactory.create(loadConfig().getConfig("values"), classOf[ValuesConfig])
        assertNotNull(recordConfig)
        assertEquals(42, recordConfig.obj)
        assertEquals("abcd", recordConfig.config.getString("abcd"))
        assertEquals(3, recordConfig.configObj.toConfig.getInt("intVal"))
        assertEquals(stringValue("hello world"), recordConfig.configValue)
        assertEquals(List(1, 2, 3).map(intValue), recordConfig.list.asScala)
        assertEquals(true, recordConfig.unwrappedMap.get("shouldBeInt"))
        assertEquals(42, recordConfig.unwrappedMap.get("should-be-boolean"))
    }

    @Test
    def testOptionalProperties() {
        val recordConfig: ObjectsConfig = ConfigRecordFactory.create(loadConfig().getConfig("objects"), classOf[ObjectsConfig])
        assertNotNull(recordConfig)
        assertNotNull(recordConfig.valueObject)
        assertNull(recordConfig.valueObject.optionalValue)
        assertTrue(recordConfig.valueObject.Default.isEmpty)
        assertEquals("notNull", recordConfig.valueObject.mandatoryValue)
    }

    @Test
    def testNotAnOptionalProperty(): Unit = {
        val e = intercept[ConfigException.ValidationFailed] {
            ConfigRecordFactory.create(parseConfig("{valueObject: {}}"), classOf[ObjectsConfig])
        }
        assertTrue("missing value error", e.getMessage.contains("No setting"))
        assertTrue("error about the right property", e.getMessage.contains("mandatoryValue"))

    }

    @Test
    def testNotABeanField() {
        val e = intercept[BadRecord] {
            ConfigRecordFactory.create(parseConfig("notBean=42"), classOf[NotABeanFieldConfig])
        }
        assertTrue("unsupported type error", e.getMessage.contains("Unsupported type"))
        assertTrue("error about the right property", e.getMessage.contains("notBean"))
    }

    @Test
    def testNotAnEnumField() {
        val e = intercept[ConfigException.BadValue] {
            ConfigRecordFactory.create(parseConfig("{problem=P1,solutions=[S4]}"), classOf[EnumsConfig])
        }
        assertTrue("invalid value error", e.getMessage.contains("Invalid value"))
        assertTrue("error about the right property", e.getMessage.contains("solutions"))
        assertTrue("error enumerates the enum constants", e.getMessage.contains("should be one of [S1, S2, S3]"))
    }

    @Test
    def testUnsupportedListElement() {
        val e = intercept[ConfigRecordException.BadRecord] {
            ConfigRecordFactory.create(parseConfig("uri=[42]"), classOf[UnsupportedListElementConfig])
        }
        assertTrue("unsupported element type error", e.getMessage.contains("Unsupported list element type"))
        assertTrue("error about the right property", e.getMessage.contains("uri"))
    }

    @Test
    def testDifferentFieldNameFromAccessors(): Unit = {
        val e = intercept[ConfigException.ValidationFailed] {
            ConfigRecordFactory.create(ConfigFactory.empty(), classOf[DifferentFieldNameFromAccessorsConfig])
        }
        assertTrue("only one missing value error", e.getMessage.contains("No setting"))
    }

    @Test
    def testMapConfig(): Unit = {
        val conf =
            """
              |map-of-string = { a = "A", b = "B" }
              |map-of-int = { a = 1, b = 2 }
              |map-of-record = {
              |  one = { abcd = "a", yes = "y" }
              |  two = { abcd = "b", yes = "y" }
              |}
              |""".stripMargin
        val recordConfig = ConfigRecordFactory.create(parseConfig(conf), classOf[MapConfig])
        assertEquals(Map("a" -> "A", "b" -> "B").asJava, recordConfig.mapOfString)
        assertEquals(Map("a" -> 1, "b" -> 2).asJava, recordConfig.mapOfInt)
        assertEquals(new StringsConfig("b", "y"), recordConfig.mapOfRecord.get("two"))
    }

    @Test
    def testBeanConfig(): Unit = {
        val recordConfig = ConfigRecordFactory.create(loadConfig(), classOf[BeanConfig])
        assertEquals("hello", recordConfig.bean.getString)
        assertEquals(42, recordConfig.bean.getInteger)
        assertTrue(recordConfig.optionalBean.isPresent)
        assertEquals("hello", recordConfig.optionalBean.get.getString)
        assertEquals(42, recordConfig.optionalBean.get.getInteger)
        assertEquals(2, recordConfig.beanList.size)
        assertEquals("hello", recordConfig.beanList.get(0).getString)
        // two beans with same fields are not equal
        assertEquals(2, recordConfig.beanSet.size)
        assertEquals("hello", recordConfig.beanSet.iterator.next.getString)
        assertEquals(2, recordConfig.beanMap.size)
        assertEquals("world", recordConfig.beanMap.get("bean2").getString)
    }

    @Test
    def testOptionalRecordConfig(): Unit = {
        val recordConfig = ConfigRecordFactory.create(loadConfig().getConfig("optionals"), classOf[OptionalsConfig])
        assertTrue(recordConfig.empty.isEmpty)
        assertTrue(recordConfig.ofInt.isPresent)
        assertEquals(1, recordConfig.ofInt.get)
        assertTrue(recordConfig.ofString.isPresent)
        assertEquals("a", recordConfig.ofString.get)
        assertTrue(recordConfig.ofDouble.isPresent)
        assertEquals(3.14, recordConfig.ofDouble.get, 1e-6)
        assertTrue(recordConfig.ofLong.isPresent)
        assertEquals(32L, recordConfig.ofLong.get)
        assertFalse(recordConfig.ofNull.isPresent)
        assertTrue(recordConfig.ofBoolean.isPresent)
        assertEquals(true, recordConfig.ofBoolean.get)
        assertTrue(recordConfig.ofObject.isPresent)
        assertTrue(recordConfig.ofObject.get.isInstanceOf[java.util.Map[_,_]])
        assertTrue(recordConfig.ofConfig.isPresent)
        assertEquals(3, recordConfig.ofConfig.get.getInt("intVal"))
        assertTrue(recordConfig.ofConfigObject.isPresent)
        assertEquals(3, recordConfig.ofConfigObject.get.toConfig.getInt("intVal"))
        assertTrue(recordConfig.ofConfigValue.isPresent)
        assertEquals(intValue(1), recordConfig.ofConfigValue.get)
        assertTrue(recordConfig.ofDuration.isPresent)
        assertEquals(Duration.ofMillis(1), recordConfig.ofDuration.get)
        assertTrue(recordConfig.ofMemorySize.isPresent)
        assertEquals(ConfigMemorySize.ofBytes(1024), recordConfig.ofMemorySize.get)
        assertTrue(recordConfig.ofStringRecord.isPresent)
        assertEquals("testAbcdOne", recordConfig.ofStringRecord.get.abcd)
        assertEquals("testYesOne", recordConfig.ofStringRecord.get.yes)
        assertTrue(recordConfig.ofStringList.isPresent)
        assertEquals(List("a", "b").asJava, recordConfig.ofStringList.get)
        assertTrue(recordConfig.ofStringSet.isPresent)
        assertEquals(Set("a", "b").asJava, recordConfig.ofStringSet.get)
        assertTrue(recordConfig.ofStringMap.isPresent)
        assertEquals(Map("a" -> "x", "b" -> "y").asJava, recordConfig.ofStringMap.get)
        assertTrue(recordConfig.ofEnum.isPresent)
        assertEquals(Problem.P1, recordConfig.ofEnum.get)
        assertTrue(recordConfig.ofConfigList.isPresent)
        assertEquals(2, recordConfig.ofConfigList.get.size)
    }

    @Test
    def testMapsConfig(): Unit = {
        val recordConfig = ConfigRecordFactory.create(loadConfig().getConfig("maps"), classOf[MapsConfig])
        assertEquals("value1", recordConfig.stringMap.get("key1"))
        assertEquals(1, recordConfig.intMap.get("key1"))
        assertEquals(1.1, recordConfig.doubleMap.get("key1"), 1e-6)
        assertEquals(100L, recordConfig.longMap.get("key1"))
        assertTrue(recordConfig.booleanMap.get("key1"))
        assertEquals(Duration.ofSeconds(10), recordConfig.durationMap.get("key1"))
        assertEquals(ConfigMemorySize.ofBytes(10 * 1024 * 1024), recordConfig.memoryMap.get("key1"))
        assertEquals(2, recordConfig.objectMap.get("key1").size)
        assertEquals("abcd", recordConfig.configMap.get("key1").getString("abcd"))
        assertEquals(stringValue("value1"), recordConfig.configValueMap.get("key1"))
        assertEquals(List(1, 2, 3).asJava, recordConfig.listMap.get("key1"))
        assertEquals("hello", recordConfig.beanMap.get("bean1").getString)
        assertEquals(2, recordConfig.configListMap.get("key1").size)
        assertEquals(EnumsConfig.Solution.S1, recordConfig.enumMap.get("key1"))
    }

    @Test
    def testUnsupportedMapKey(): Unit = {
        val e = intercept[BadRecord] {
            ConfigRecordFactory.create(loadConfig(), classOf[UnsupportedMapKeyConfig])
        }
        assertTrue(e.getMessage.contains("Unsupported map key type"))
    }

    @Test
    def testUnsupportedMapValue(): Unit = {
        val e = intercept[BadRecord] {
            ConfigRecordFactory.create(loadConfig(), classOf[UnsupportedMapValueConfig])
        }
        assertTrue(e.getMessage.contains("Unsupported map value type"))
    }

    @Test
    def testUnsupportedOptionalValue(): Unit = {
        val e = intercept[BadRecord] {
            ConfigRecordFactory.create(loadConfig(), classOf[UnsupportedOptionalValueConfig])
        }
        assertTrue(e.getMessage.contains("Unsupported optional type"))
        assertTrue(e.getMessage.contains("unsupportedOptionalValue"))
    }

    private def loadConfig(): Config = {
        val configIs: InputStream = this.getClass().getClassLoader().getResourceAsStream("recordconfig/recordconfig01.conf")
        try {
            val config: Config = ConfigFactory.parseReader(new InputStreamReader(configIs),
              ConfigParseOptions.defaults.setSyntax(ConfigSyntax.CONF)).resolve
          config
        } finally {
          configIs.close()
        }
    }


    protected def checkValidationException(e: ConfigException.ValidationFailed, expecteds: Seq[Problem]) {
        val problems = e.problems().asScala.toIndexedSeq.sortBy(_.path).sortBy(_.origin.lineNumber)

        for ((problem, expected) <- problems zip expecteds) {
            expected.check(problem)
        }
        assertEquals("found expected validation problems, got '" + problems + "' and expected '" + expecteds + "'",
            expecteds.size, problems.size)
    }

    protected def intercept[E <: Throwable: ClassTag](block: => Any): E = {
        val expectedClass = classTag[E].runtimeClass
        var thrown: Option[Throwable] = None
        val result = try {
            Some(block)
        } catch {
            case t: Throwable =>
                thrown = Some(t)
                None
        }
        thrown match {
            case Some(t) if expectedClass.isAssignableFrom(t.getClass) =>
                t.asInstanceOf[E]
            case Some(t) =>
                throw new Exception(s"Expected exception ${expectedClass.getName} was not thrown, got $t", t)
            case None =>
                throw new Exception(s"Expected exception ${expectedClass.getName} was not thrown, no exception was thrown and got result $result")
        }
    }

    sealed abstract class Problem(path: String, line: Int) {
        def check(p: ConfigException.ValidationProblem) {
            assertEquals("matching path", path, p.path())
            assertEquals("matching line for " + path, line, p.origin().lineNumber())
        }

        protected def assertMessage(p: ConfigException.ValidationProblem, re: String) {
            assertTrue("didn't get expected message for " + path + ": got '" + p.problem() + "'",
                p.problem().matches(re))
        }
    }

    case class Missing(path: String, line: Int, expected: String) extends Problem(path, line) {
        override def check(p: ConfigException.ValidationProblem) {
            super.check(p)
            val re = "No setting.*" + path + ".*expecting.*" + expected + ".*"
            assertMessage(p, re)
        }
    }

    case class WrongType(path: String, line: Int, expected: String, got: String) extends Problem(path, line) {
        override def check(p: ConfigException.ValidationProblem) {
            super.check(p)
            val re = "Wrong value type.*" + path + ".*expecting.*" + expected + ".*got.*" + got + ".*"
            assertMessage(p, re)
        }
    }

    // it's important that these do NOT use the public API to create the
    // instances, because we may be testing that the public API returns the
    // right instance by comparing to these, so using public API here would
    // make the test compare public API to itself.
    protected def intValue(i: Int) = new ConfigInt(fakeOrigin(), i, null)
    protected def longValue(l: Long) = new ConfigLong(fakeOrigin(), l, null)
    protected def boolValue(b: Boolean) = new ConfigBoolean(fakeOrigin(), b)
    protected def nullValue = new ConfigNull(fakeOrigin())
    protected def stringValue(s: String) = new ConfigString.Quoted(fakeOrigin(), s)
    protected def doubleValue(d: Double) = new ConfigDouble(fakeOrigin(), d, null)

    def fakeOrigin() = {
        SimpleConfigOrigin.newSimple("fake origin")
    }

    protected def parseConfig(s: String) = {
        val options = ConfigParseOptions.defaults().
          setOriginDescription("test string").
          setSyntax(ConfigSyntax.CONF);
        ConfigFactory.parseString(s, options).asInstanceOf[SimpleConfig]
    }

}
